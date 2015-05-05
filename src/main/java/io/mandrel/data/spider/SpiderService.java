package io.mandrel.data.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.common.data.State;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.data.filters.link.AllowedForDomainsFilter;
import io.mandrel.data.filters.link.SkipAncorFilter;
import io.mandrel.data.filters.link.UrlPatternFilter;
import io.mandrel.data.source.FixedSource;
import io.mandrel.data.source.Source;
import io.mandrel.gateway.Document;
import io.mandrel.http.Requester;
import io.mandrel.http.WebPage;
import io.mandrel.task.TaskService;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.kohsuke.randname.RandomNameGenerator;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpiderService {

	private final SpiderRepository spiderRepository;

	private final TaskService taskService;

	private final ExtractorService extractorService;

	private final Requester requester;

	private final HazelcastInstance instance;

	private Validator spiderValidator = new SpiderValidator();

	private RandomNameGenerator generator = new RandomNameGenerator();

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

	public void injectAndInit(Spider spider) {
		spider.getStores().getPageMetadataStore().setHazelcastInstance(instance);
		if (spider.getStores().getPageStore() != null) {
			spider.getStores().getPageStore().setHazelcastInstance(instance);
		}

		// TODO
		Map<String, Object> properties = new HashMap<>();

		spider.getStores().getPageMetadataStore().init(properties);
		spider.getStores().getPageStore().init(properties);
	}

	public BindingResult validate(Spider spider) {
		BindingResult errors = new BeanPropertyBindingResult(spider, "spider");
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
	public Spider add(List<String> urls) throws BindException {
		Spider spider = new Spider();
		spider.setName(generator.next());

		// Add source
		spider.setSources(Arrays.asList(new FixedSource().setUrls(urls).setName("fixed_source")));

		// Add filters
		spider.getFilters().getForLinks().add(new AllowedForDomainsFilter().setDomains(urls.stream().map(url -> {
			try {
				return new URL(url).getHost();
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}).collect(Collectors.toList())));
		spider.getFilters().getForLinks().add(new SkipAncorFilter());
		spider.getFilters().getForLinks().add(UrlPatternFilter.STATIC);

		return add(spider);
	}

	public Spider add(Spider spider) throws BindException {
		BindingResult errors = validate(spider);

		if (errors.hasErrors()) {
			errors.getAllErrors().stream().forEach(oe -> log.info(oe.toString()));
			throw new BindException(errors);
		}

		spider = spiderRepository.add(spider);

		return spider;
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
				taskService.executeOnRandomMember(sourceExecServiceName, new SourceTask(spider.getId(), source));
			} else {
				log.debug("Sourcing from all members");
				taskService.executeOnAllMembers(sourceExecServiceName, new SourceTask(spider.getId(), source));
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

		injectAndInit(spider);
		
		Analysis report = new Analysis();
		if (spider.getExtractors() != null) {
			if (spider.getExtractors().getPages() != null) {
				Map<String, List<Document>> documentsByExtractor = spider.getExtractors().getPages().stream()
						.map(ex -> Pair.of(ex.getName(), extractorService.extractThenFormat(webPage, ex)))
						.collect(Collectors.toMap(key -> key.getLeft(), value -> value.getRight()));
				report.setDocuments(documentsByExtractor);
			}
			if (spider.getExtractors().getOutlinks() != null) {
				Map<String, Pair<Set<Link>, Set<String>>> outlinksByExtractor = spider.getExtractors().getOutlinks().stream().map(ol -> {
					return Pair.of(ol.getName(), extractorService.extractAndFilterOutlinks(spider, webPage.getUrl().toString(), webPage, ol));
				}).collect(Collectors.toMap(key -> key.getLeft(), value -> value.getRight()));

				report.setOutlinks(Maps.transformEntries(outlinksByExtractor, (key, entries) -> entries.getLeft()));
				report.setFilteredOutlinks(Maps.transformEntries(outlinksByExtractor, (key, entries) -> entries.getRight()));
			}
		}

		report.setMetadata(webPage.getMetadata());
		return report;
	}
}
