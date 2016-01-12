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
package io.mandrel.spider;

import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.cluster.instance.StateService;
import io.mandrel.common.MandrelException;
import io.mandrel.common.NotFoundException;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Statuses;
import io.mandrel.common.service.TaskContext;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.thrift.Clients;
import io.mandrel.data.filters.link.AllowedForDomainsFilter;
import io.mandrel.data.filters.link.SkipAncorFilter;
import io.mandrel.data.filters.link.UrlPatternFilter;
import io.mandrel.data.source.FixedSource.FixedSourceDefinition;
import io.mandrel.data.source.Source;
import io.mandrel.data.validation.Validators;
import io.mandrel.metrics.Accumulators;
import io.mandrel.metrics.MetricsRepository;
import io.mandrel.timeline.Event;
import io.mandrel.timeline.Event.SpiderInfo.SpiderEventType;
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
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

@Component
@Slf4j
public class SpiderService {

	@Autowired
	private SpiderRepository spiderRepository;
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
	@Autowired
	private ObjectMapper objectMapper;

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
			List<Spider> spiders = spiderRepository.listActive().collect(Collectors.toList());
			SyncRequest sync = new SyncRequest();
			sync.setDefinitions(spiders.stream().map(spider -> {
				try {
					return objectMapper.writeValueAsBytes(spider);
				} catch (Exception e) {
					throw Throwables.propagate(e);
				}
			}).collect(Collectors.toList()));

			if (CollectionUtils.isNotEmpty(spiders)) {
				discoveryClient.getInstances(ServiceIds.WORKER).forEach(instance -> {
					try {
						clients.onWorker(HostAndPort.fromParts(instance.getHost(), instance.getPort())).with(worker -> worker.sync(sync));
					} catch (Exception e) {
						log.warn("Can not sync due to", e);
					}
				});
				discoveryClient.getInstances(ServiceIds.FRONTIER).forEach(instance -> {
					try {
						clients.onFrontier(HostAndPort.fromParts(instance.getHost(), instance.getPort())).with(frontier -> frontier.sync(sync));
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

		updateTimeline(spider, SpiderEventType.SPIDER_STARTED);

		return spiderRepository.update(spider);
	}

	public void updateTimeline(Spider spider, SpiderEventType status) {
		Event event = Event.forSpider();
		event.getSpider().setSpiderId(spider.getId()).setSpiderName(spider.getName()).setType(status);
		timelineService.add(event);
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
		spider.getFilters().getLinks().add(new AllowedForDomainsFilter().domains(urls.stream().map(url -> {
			return URI.create(url).getHost();
		}).collect(Collectors.toList())));
		spider.getFilters().getLinks().add(new SkipAncorFilter());
		spider.getFilters().getLinks().add(UrlPatternFilter.STATIC);

		return add(spider);
	}

	public long fork(long id) throws BindException {
		Spider spider = get(id);
		spider.setId(0);
		spider.setName(generator.next());

		cleanDates(spider);

		BindingResult errors = Validators.validate(spider);

		if (errors.hasErrors()) {
			errors.getAllErrors().stream().forEach(oe -> log.info(oe.toString()));
			throw new BindException(errors);
		}

		spider.setStatus(Statuses.CREATED);
		spider.setCreated(LocalDateTime.now());

		spider = spiderRepository.add(spider);

		return spider.getId();
	}

	public void cleanDates(Spider spider) {
		spider.setCreated(null);
		spider.setDeleted(null);
		spider.setEnded(null);
		spider.setKilled(null);
		spider.setPaused(null);
		spider.setStarted(null);
	}

	public Spider add(Spider spider) throws BindException {
		BindingResult errors = Validators.validate(spider);

		if (errors.hasErrors()) {
			errors.getAllErrors().stream().forEach(oe -> log.info(oe.toString()));
			throw new BindException(errors);
		}

		spider.setStatus(Statuses.CREATED);
		spider.setCreated(LocalDateTime.now());
		spider = spiderRepository.add(spider);

		updateTimeline(spider, SpiderEventType.SPIDER_CREATED);

		return spider;
	}

	public Spider get(long id) {
		return spiderRepository.get(id).orElseThrow(() -> new NotFoundException("Spider not found"));
	}

	public Stream<Spider> list() {
		return spiderRepository.list();
	}

	public Stream<Spider> listActive() {
		return spiderRepository.listActive();
	}

	public void reinject(long spiderId) {
		Spider spider = get(spiderId);
		injectSingletonSources(spider);
	}

	public void start(long spiderId) {
		Spider spider = get(spiderId);

		if (Statuses.STARTED.equals(spider.getStatus())) {
			return;
		}

		if (Statuses.KILLED.equals(spider.getStatus())) {
			throw new MandrelException("Spider cancelled!");
		}

		// Can not start a spider if there no frontier started
		if (discoveryClient.getInstances(ServiceIds.FRONTIER).size() < 1) {
			throw new MandrelException("Can not start spider, you need a least a frontier instance!");
		}

		if (Statuses.CREATED.equals(spider.getStatus())) {
			injectSingletonSources(spider);
		}

		spiderRepository.updateStatus(spiderId, Statuses.STARTED);

		updateTimeline(spider, SpiderEventType.SPIDER_CREATED);

	}

	public void injectSingletonSources(Spider spider) {
		// Deploy singleton sources on a random frontier
		TaskContext context = new TaskContext();
		context.setDefinition(spider);

		spider.getSources().forEach(
				s -> {
					Source source = s.build(context);

					if (source.singleton() && source.check()) {
						log.debug("Injecting source '{}' ({})", s.name(), s.toString());
						ServiceInstance instance = discoveryClient.getInstances(ServiceIds.FRONTIER).get(0);

						source.register(uri -> {
							try {
								log.trace("Adding uri '{}'", uri);
								clients.onFrontier(HostAndPort.fromParts(instance.getHost(), instance.getPort())).with(
										frontier -> frontier.schedule(spider.getId(), uri));
							} catch (Exception e) {
								log.warn("Can not sync due to", e);
							}
						});
					}
				});
	}

	public void pause(long spiderId) {
		Spider spider = get(spiderId);

		// Update status
		spiderRepository.updateStatus(spiderId, Statuses.PAUSED);

		updateTimeline(spider, SpiderEventType.SPIDER_PAUSED);

	}

	public void kill(long spiderId) {
		Spider spider = get(spiderId);

		// Update status
		spiderRepository.updateStatus(spiderId, Statuses.KILLED);

		updateTimeline(spider, SpiderEventType.SPIDER_KILLED);
	}

	public void delete(long spiderId) {
		Spider spider = get(spiderId);

		// Update status
		spiderRepository.updateStatus(spiderId, Statuses.DELETED);

		updateTimeline(spider, SpiderEventType.SPIDER_DELETED);

		metricsRepository.delete(spiderId);

	}
}
