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
import io.mandrel.cluster.instance.StateService;
import io.mandrel.common.NotFoundException;
import io.mandrel.common.client.Clients;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Statuses;
import io.mandrel.common.sync.SyncRequest;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.kohsuke.randname.RandomNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

@Component
@Slf4j
public class ControllerService {

	@Autowired
	private ControllerRepository controllerRepository;
	@Autowired
	private TimelineService timelineService;
	@Autowired
	private Clients clients;
	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private StateService stateService;
	@Autowired
	private Accumulators accumulators;
	@Autowired
	private MetricsRepository metricsRepository;

	private RandomNameGenerator generator = new RandomNameGenerator();

	@PostConstruct
	public void init() {
		// TODO Load the journal of commands
	}

	@Scheduled(fixedRate = 10000)
	public void sync() {
		if (stateService.isStarted()) {
			// TODO HOW TO in case of multiple controller
			log.debug("Syncing the nodes from the controller...");
			// Load the existing spiders from the database
			List<Spider> spiders = controllerRepository.listActive().collect(Collectors.toList());
			SyncRequest sync = new SyncRequest();
			sync.setSpiders(spiders);

			if (CollectionUtils.isNotEmpty(spiders)) {
				discoveryClient.getInstances(ServiceIds.WORKER).forEach(worker -> {
					try {
						clients.workerClient().sync(sync, worker.getUri());
					} catch (Exception e) {
						log.warn("Can not sync due to", e);
					}
				});
				discoveryClient.getInstances(ServiceIds.FRONTIER).forEach(worker -> {
					try {
						clients.frontierClient().sync(sync, worker.getUri());
					} catch (Exception e) {
						log.warn("Can not sync due to", e);
					}
				});
			} else {
				log.debug("Nothing to sync...");
			}
		}
	}

	public Spider update(Spider spider) throws BindException {
		BindingResult errors = Validators.validate(spider);

		if (errors.hasErrors()) {
			errors.getAllErrors().stream().forEach(oe -> log.info(oe.toString()));
			throw new BindException(errors);
		}

		timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_STARTED)
				.setTime(LocalDateTime.now()));

		return controllerRepository.update(spider);
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

		spider.setStatus(Statuses.CREATED);
		spider.setCreated(LocalDateTime.now());
		spider = controllerRepository.add(spider);

		timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_CREATED)
				.setTime(LocalDateTime.now()));

		return spider;
	}

	public Spider get(long id) {
		return controllerRepository.get(id).orElseThrow(() -> new NotFoundException("Spider not found"));
	}

	public Stream<Spider> list() {
		return controllerRepository.list();
	}

	public Stream<Spider> listActive() {
		return controllerRepository.listActive();
	}

	public void start(long spiderId) {
		Spider spider = get(spiderId);

		if (Statuses.STARTED.equals(spider.getStatus())) {
			return;
		}

		if (Statuses.KILLED.equals(spider.getStatus())) {
			throw new RuntimeException("Spider cancelled!");
		}

		controllerRepository.updateStatus(spiderId, Statuses.STARTED);
		timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_STARTED)
				.setTime(LocalDateTime.now()));

		// TODO Deploy singleton sources on a random frontier

	}

	public void pause(long spiderId) {
		Spider spider = get(spiderId);

		// Update status
		controllerRepository.updateStatus(spiderId, Statuses.PAUSED);
		timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_PAUSED)
				.setTime(LocalDateTime.now()));

	}

	public void kill(long spiderId) {
		Spider spider = get(spiderId);

		// Update status
		controllerRepository.updateStatus(spiderId, Statuses.KILLED);
		timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_KILLED)
				.setTime(LocalDateTime.now()));

	}

	public void delete(long spiderId) {
		Spider spider = get(spiderId);

		// Update status
		controllerRepository.updateStatus(spiderId, Statuses.DELETED);
		timelineService.add(new SpiderEvent().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(SpiderEventType.SPIDER_DELETED)
				.setTime(LocalDateTime.now()));

		metricsRepository.delete(spiderId);

	}
}
