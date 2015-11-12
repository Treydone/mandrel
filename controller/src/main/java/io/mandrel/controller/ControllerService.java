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
package io.mandrel.controller;

import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.command.Commands;
import io.mandrel.command.Commands.CommandGroup;
import io.mandrel.common.client.Clients;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.State;
import io.mandrel.data.filters.link.AllowedForDomainsFilter;
import io.mandrel.data.filters.link.SkipAncorFilter;
import io.mandrel.data.filters.link.UrlPatternFilter;
import io.mandrel.data.source.FixedSource.FixedSourceDefinition;
import io.mandrel.data.validation.Validators;
import io.mandrel.metrics.Accumulators;
import io.mandrel.metrics.MetricsRepository;
import io.mandrel.timeline.SpiderEvent;
import io.mandrel.timeline.SpiderEvent.SpiderEventType;
import io.mandrel.timeline.TimelineService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.kohsuke.randname.RandomNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

@Component
@Slf4j
public class ControllerService {

	@Autowired
	private ControllerRepository spiderRepository;
	@Autowired
	private TimelineService timelineService;
	@Autowired
	private Clients clients;
	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;
	@Autowired
	private Accumulators accumulators;
	@Autowired
	private MetricsRepository metricsRepository;

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

		// TODO Load the journal of commands

		scheduledExecutorService.scheduleAtFixedRate(() -> sync(), 0, 10000, TimeUnit.MILLISECONDS);
	}

	public void sync() {
		// Load the existing spiders from the database
		List<Spider> spiders = spiderRepository.listActive().collect(Collectors.toList());
		discoveryClient.getInstances(ServiceIds.WORKER).forEach(worker -> {
			clients.workerClient().sync(spiders, worker.getUri());
		});
		discoveryClient.getInstances(ServiceIds.FRONTIER).forEach(worker -> {
			clients.frontierClient().sync(spiders, worker.getUri());
		});
	}

	public Spider update(Spider spider) throws BindException {
		BindingResult errors = Validators.validate(spider);

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
		FixedSourceDefinition source = new FixedSourceDefinition();
		source.setUrls(urls);
		spider.setSources(Arrays.asList(source));

		// Add filters
		spider.getFilters().getForLinks().add(new AllowedForDomainsFilter().domains(urls.stream().map(url -> {
			return URI.create(url).getHost();
		}).collect(Collectors.toList())));
		spider.getFilters().getForLinks().add(new SkipAncorFilter());
		spider.getFilters().getForLinks().add(UrlPatternFilter.STATIC);

		return add(spider);
	}

	public Spider add(Spider spider) throws BindException {
		BindingResult errors = Validators.validate(spider);

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

	public Stream<Spider> listActive() {
		return spiderRepository.listActive();
	}

	public Optional<Spider> start(long spiderId) {
		return get(spiderId).map(
				spider -> {

					if (State.STARTED.equals(spider.getState())) {
						return spider;
					}

					if (State.KILLED.equals(spider.getState())) {
						throw new RuntimeException("Spider cancelled!");
					}

					CommandGroup startCommand = Commands.groupOf(
					// Create the dedicated frontier
							Commands.prepareFrontier(discoveryClient, clients.frontierClient(), spider),
							// Deploy the spider on the workers
							Commands.prepareWorker(discoveryClient, clients.workerClient(), spider),
							// Start the frontier
							Commands.startFrontier(discoveryClient, clients.frontierClient(), spider),
							// Start the workers
							Commands.startWorker(discoveryClient, clients.workerClient(), spider));

					startCommand.apply();

					spider.setState(State.STARTED);
					spider.setStarted(LocalDateTime.now());
					spiderRepository.update(spider);

					timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_STARTED)
							.setTime(LocalDateTime.now()));

					// TODO Deploy singleton sources on a random frontier

					return spider;

				});
	}

	public Optional<Spider> kill(long spiderId) {
		return get(spiderId).map(spider -> {

			CommandGroup killCommand = Commands.groupOf(
			// Kill the workers
					Commands.killWorker(discoveryClient, clients.workerClient(), spider),
					// Kill the frontier
					Commands.killFrontier(discoveryClient, clients.frontierClient(), spider));

			killCommand.apply();

			// Update status
				spider.setCancelled(LocalDateTime.now());
				spider.setState(State.KILLED);

				timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_KILLED)
						.setTime(LocalDateTime.now()));

				return spiderRepository.update(spider);
			});
	}

	public Optional<Spider> delete(long spiderId) {
		return get(spiderId).map(spider -> {

			CommandGroup killCommand = Commands.groupOf(
			// Kill the workers
					Commands.killWorker(discoveryClient, clients.workerClient(), spider),
					// Kill the frontier
					Commands.killFrontier(discoveryClient, clients.frontierClient(), spider));

			killCommand.apply();

			// Update status
				spider.setCancelled(LocalDateTime.now());
				spider.setState(State.DELETED);

				timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_DELETED)
						.setTime(LocalDateTime.now()));

				metricsRepository.delete(spiderId);

				return spiderRepository.update(spider);
			});
	}
}
