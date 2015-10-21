/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.data.spider;

import io.mandrel.blob.BlobStores;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.State;
import io.mandrel.data.filters.link.AllowedForDomainsFilter;
import io.mandrel.data.filters.link.SkipAncorFilter;
import io.mandrel.data.filters.link.UrlPatternFilter;
import io.mandrel.data.source.FixedSource.FixedSourceDefinition;
import io.mandrel.document.DocumentStores;
import io.mandrel.timeline.SpiderEvent;
import io.mandrel.timeline.SpiderEvent.SpiderEventType;
import io.mandrel.timeline.TimelineService;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.kohsuke.randname.RandomNameGenerator;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import com.google.common.base.Throwables;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpiderService {

	private final SpiderRepository spiderRepository;

	private final TimelineService timelineService;

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

		// TODO Start the available spiders on this node
		// spiderRepository.list().filter(spider ->
		// State.STARTED.equals(spider.getState())).forEach(spider -> {
		// taskService.executeOnLocalMember(String.valueOf(spider.getId()), new
		// SpiderTask(spider));

		// TODO manage sources!œ
		// spider.getSources().stream().forEach(prepareSource(spider.getId(),
		// spider));
		// });
	}

	public BindingResult validate(Spider spider) {
		BindingResult errors = new BeanPropertyBindingResult(spider, "spider");
		spiderValidator.validate(spider, errors);
		return errors;
	}

	public Spider update(Spider spider) throws BindException {
		BindingResult errors = validate(spider);

		if (errors.hasErrors()) {
			errors.getAllErrors().stream().forEach(oe -> log.info(oe.toString()));
			throw new BindException(errors);
		}

		timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_STARTED)
				.setTime(LocalDateTime.now()));

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
		FixedSourceDefinition source = new FixedSourceDefinition().urls(urls);
		spider.setSources(Arrays.asList(source));

		// Add filters
		spider.getFilters().getForLinks().add(new AllowedForDomainsFilter().domains(urls.stream().map(url -> {
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

		spider.setAdded(LocalDateTime.now());
		spider = spiderRepository.add(spider);

		timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_NEW)
				.setTime(LocalDateTime.now()));

		return spider;
	}

	public Optional<Spider> get(long id) {
		return spiderRepository.get(id);
	}

	public Stream<Spider> list() {
		return spiderRepository.list();
	}

	public Optional<Spider> start(long spiderId) {
		return get(spiderId).map(spider -> {

			if (State.STARTED.equals(spider.getState())) {
				return spider;
			}

			if (State.CANCELLED.equals(spider.getState())) {
				throw new RuntimeException("Spider cancelled!");
			}

			// TODO
			// if (spider.getClient().getPoliteness().isUseSitemaps()) {
			// spider.getSources().add(new SitemapsSource(url));
			// }

				// spider.getSources().stream().filter(s ->
				// s.check()).forEach(prepareSource(spiderId, spider));

				// taskService.prepareSimpleExecutor(String.valueOf(spiderId));

				// Call workers
				// taskService.executeOnAllMembers(String.valueOf(spiderId), new
				// SpiderTask(spider));

				spider.setState(State.STARTED);
				spider.setStarted(LocalDateTime.now());
				spiderRepository.update(spider);

				timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_STARTED)
						.setTime(LocalDateTime.now()));

				return spider;

			});
	}

	public Optional<Spider> cancel(long spiderId) {
		return get(spiderId).map(spider -> {

			// TODO taskService.shutdownAllExecutorService(spider);

				// Update status
				spider.setCancelled(LocalDateTime.now());
				spider.setState(State.CANCELLED);

				timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_CANCELLED)
						.setTime(LocalDateTime.now()));

				return spiderRepository.update(spider);
			});
	}

	public Optional<Spider> end(long spiderId) {
		return get(spiderId).map(spider -> {

			// TODO taskService.shutdownAllExecutorService(spider);

				// Update status
				spider.setEnded(LocalDateTime.now());
				spider.setState(State.ENDED);
				return spiderRepository.update(spider);
			});
	}

	public Optional<Spider> delete(long spiderId) {
		return get(spiderId).map(spider -> {
			// TODO taskService.shutdownAllExecutorService(spider);

				// Delete data
				BlobStores.get(spiderId).ifPresent(b -> b.deleteAll());
				DocumentStores.get(spiderId).ifPresent(d -> d.entrySet().forEach(e -> {
					e.getValue().deleteAll();
				}));

				// Remove spider
				spiderRepository.delete(spiderId);

				return spider;
			});
	}
}
