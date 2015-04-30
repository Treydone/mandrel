package io.mandrel.data.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.common.data.State;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.data.source.FixedSource;
import io.mandrel.data.source.Source;
import io.mandrel.gateway.Document;
import io.mandrel.http.Requester;
import io.mandrel.http.WebPage;
import io.mandrel.task.TaskService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

@Component
@Slf4j
public class SpiderService {

	private final SpiderRepository spiderRepository;

	private final TaskService taskService;

	private final ExtractorService extractorService;

	private final Requester requester;

	private Validator spiderValidator = new SpiderValidator();

	@Inject
	public SpiderService(SpiderRepository spiderRepository, TaskService taskService, ExtractorService extractorService, Requester requester) {
		this.spiderRepository = spiderRepository;
		this.taskService = taskService;
		this.extractorService = extractorService;
		this.requester = requester;
	}

	/**
	 * On start:
	 * <ul>
	 * <li>Find the available spiders, and foreach spider:
	 * <li>Start the spider
	 * <li>Start the sources
	 * </ul>
	 */
	@PostConstruct
	public void init() {

		// Start the available spiders on this node
		spiderRepository.list().filter(spider -> State.STARTED.equals(spider.getState())).forEach(spider -> {
			taskService.executeOnLocalMember(String.valueOf(spider.getId()), new SpiderTask(spider));

			// TODO manage sources!
			// spider.getSources().stream().forEach(prepareSource(spider.getId(),
			// spider));
			});
	}

	public Errors validate(Spider spider) {
		Errors errors = new BeanPropertyBindingResult(spider, "spider");
		spiderValidator.validate(spider, errors);
		return errors;
	}

	public Spider update(Spider spider) {

		Errors errors = validate(spider);

		if (errors.hasErrors()) {
			// TODO
			throw new RuntimeException("Errors!");
		}

		return spiderRepository.update(spider);
	}

	/**
	 * Create a new spider from a fixed list of urls.
	 * 
	 * @param urls
	 * @return
	 */
	public Spider add(List<String> urls) {
		Spider spider = new Spider();
		spider.setSources(Arrays.asList(new FixedSource(urls)));
		return add(spider);
	}

	public Spider add(Spider spider) {

		Errors errors = validate(spider);

		if (errors.hasErrors()) {
			errors.getAllErrors().stream().forEach(oe -> log.info(oe.toString()));
			// TODO
			throw new RuntimeException("Errors!");
		}

		return spiderRepository.add(spider);
	}

	public Optional<Spider> get(long id) {
		return spiderRepository.get(id);
	}

	public Stream<Spider> list() {
		return spiderRepository.list();
	}

	public void start(long spiderId) {
		get(spiderId).map(spider -> {

			if (State.STARTED.equals(spider.getState())) {
				return spider;
			}

			if (State.CANCELLED.equals(spider.getState())) {
				throw new RuntimeException("Spider cancelled!");
			}

			spider.getSources().stream().filter(s -> s.check()).forEach(prepareSource(spiderId, spider));

			taskService.prepareSimpleExecutor(String.valueOf(spiderId));
			taskService.executeOnAllMembers(String.valueOf(spiderId), new SpiderTask(spider));

			spider.setState(State.STARTED);
			spiderRepository.update(spider);
			return spider;

		}).orElseThrow(() -> new RuntimeException("Unknown spider!"));
	}

	private Consumer<? super Source> prepareSource(long spiderId, Spider spider) {
		return source -> {
			String sourceExecServiceName = spiderId + "-source-" + source.getName();

			taskService.prepareSimpleExecutor(String.valueOf(sourceExecServiceName));

			if (source.singleton()) {
				log.debug("Sourcing from a random member");
				taskService.executeOnRandomMember(sourceExecServiceName, new SourceTask(spider, source));
			} else {
				log.debug("Sourcing from all members");
				taskService.executeOnAllMembers(sourceExecServiceName, new SourceTask(spider, source));
			}
		};
	}

	public void cancel(long spiderId) {
		get(spiderId).map(spider -> {

			taskService.shutdownAllExecutorService(spider);

			// Update status
				spider.setState(State.CANCELLED);
				return spiderRepository.update(spider);
			}).orElseThrow(() -> new RuntimeException("Unknown spider!"));

	}

	public void end(long spiderId) {
		get(spiderId).map(spider -> {

			taskService.shutdownAllExecutorService(spider);

			// Update status
				spider.setState(State.ENDED);
				return spiderRepository.update(spider);
			}).orElseThrow(() -> new RuntimeException("Unknown spider!"));

	}

	public void delete(long spiderId) {
		get(spiderId).map(spider -> {
			taskService.shutdownAllExecutorService(spider);

			// Delete data
				spider.getStores().getPageStore().deleteAllFor(spiderId);
				spider.getExtractors().getPages().stream().forEach(ex -> ex.getDataStore().deleteAllFor(spiderId));

				// Remove spider
				spiderRepository.delete(spiderId);

				return spider;
			}).orElseThrow(() -> new RuntimeException("Unknown spider!"));

	}

	public Analysis analyze(Long id, String source) {
		return get(id).map(spider -> {

			WebPage webPage;
			try {
				webPage = requester.getBlocking(source, spider);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
			log.trace("Getting response for {}", source);

			Analysis report = buildReport(spider, webPage);

			return report;
		}).orElseThrow(() -> new RuntimeException("Unknown spider!"));
	}

	protected Analysis buildReport(Spider spider, WebPage webPage) {
		Analysis report = new Analysis();
		if (spider.getExtractors() != null) {
			if (spider.getExtractors().getPages() != null) {
				Map<String, List<Document>> documentsByExtractor = spider.getExtractors().getPages().stream()
						.map(ex -> Pair.of(ex.getName(), extractorService.extractThenFormat(webPage, ex)))
						.collect(Collectors.toMap(key -> key.getLeft(), value -> value.getRight()));
				report.setDocuments(documentsByExtractor);
			}
			if (spider.getExtractors().getOutlinks() != null) {
				Map<String, Pair<Set<Link>, Set<Link>>> outlinksByExtractor = spider
						.getExtractors()
						.getOutlinks()
						.stream()
						.map(ol -> {
							// Find outlinks in page
							Set<Link> outlinks = extractorService.extractOutlinks(webPage, ol);

							Set<Link> filteredOutlinks = null;
							if (spider.getFilters() != null && CollectionUtils.isNotEmpty(spider.getFilters().getForLinks())) {
								filteredOutlinks = outlinks.stream().filter(link -> spider.getFilters().getForLinks().stream().anyMatch(f -> f.isValid(link)))
										.collect(Collectors.toSet());
							} else {
								filteredOutlinks = outlinks;
							}

							// Filter outlinks
							filteredOutlinks = spider.getStores().getPageMetadataStore()
									.filter(spider.getId(), filteredOutlinks, spider.getClient().getPoliteness());

							return Pair.of(ol.getName(), Pair.of(outlinks, filteredOutlinks));
						}).collect(Collectors.toMap(key -> key.getLeft(), value -> value.getRight()));

				report.setOutlinks(Maps.transformEntries(outlinksByExtractor, (key, entries) -> entries.getLeft()));
				report.setFilteredOutlinks(Maps.transformEntries(outlinksByExtractor, (key, entries) -> entries.getRight()));
			}
		}

		report.setMetadata(webPage.getMetadata());
		return report;
	}
}
