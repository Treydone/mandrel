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
package io.mandrel.endpoints.internal;

import io.mandrel.cluster.node.NodeRepository;
import io.mandrel.common.NotFoundException;
import io.mandrel.common.container.ContainerStatus;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.SpiderStatuses;
import io.mandrel.common.sync.Container;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;
import io.mandrel.controller.CoordinatorContainer;
import io.mandrel.controller.CoordinatorContainers;
import io.mandrel.endpoints.contracts.CoordinatorContract;
import io.mandrel.metrics.MetricsService;
import io.mandrel.timeline.Event;
import io.mandrel.timeline.TimelineService;
import io.mandrel.transport.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.weakref.jmx.internal.guava.base.Throwables;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class CoordinatorResource implements CoordinatorContract {

	@Autowired
	private TimelineService timelineService;
	@Autowired
	private MetricsService metricsService;
	@Autowired
	private NodeRepository nodeRepository;
	@Autowired
	private ObjectMapper objectMapper;

	private Supplier<? extends NotFoundException> coordinatorNotFound = () -> new NotFoundException("Coordinator not found");

	@Override
	public void addEvent(Event event) {
		timelineService.add(event);
	}

	@Override
	public void updateMetrics(Map<String, Long> accumulators) {
		metricsService.sync(accumulators);
	}

	@Override
	public void createCoordinatorContainer(byte[] definition) {
		Spider spider;
		try {
			spider = objectMapper.readValue(definition, Spider.class);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		create(spider);
	}

	public void create(Spider spider) {
		CoordinatorContainer container = new CoordinatorContainer(spider, null, null);
		container.register();
	}

	@Override
	public void startCoordinatorContainer(Long id) {
		CoordinatorContainers.get(id).orElseThrow(coordinatorNotFound).start();
	}

	@Override
	public void pauseCoordinatorContainer(Long id) {
		CoordinatorContainers.get(id).orElseThrow(coordinatorNotFound).pause();
	}

	@Override
	public void killCoordinatorContainer(Long id) {
		CoordinatorContainer container = CoordinatorContainers.get(id).orElseThrow(coordinatorNotFound);
		container.kill();
		container.unregister();
	}

	@Override
	public List<Container> listRunningCoordinatorContainers() {
		return CoordinatorContainers.list().stream()
				.map(f -> new Container().setSpiderId(f.spider().getId()).setVersion(f.spider().getVersion()).setStatus(f.status()))
				.collect(Collectors.toList());
	}

	@Override
	public SyncResponse syncCoordinators(SyncRequest sync) throws RemoteException {
		Collection<? extends io.mandrel.common.container.Container> containers = CoordinatorContainers.list();
		final Map<Long, Spider> spiderByIdFromCoordinator = new HashMap<>();
		if (sync.getDefinitions() != null) {
			spiderByIdFromCoordinator.putAll(sync.getDefinitions().stream().map(def -> {
				try {
					return objectMapper.readValue(def, Spider.class);
				} catch (Exception e) {
					throw Throwables.propagate(e);
				}
			}).collect(Collectors.toMap(spider -> spider.getId(), spider -> spider)));
		}

		SyncResponse response = new SyncResponse();
		List<Long> created = new ArrayList<>();
		List<Long> started = new ArrayList<>();
		List<Long> updated = new ArrayList<>();
		List<Long> killed = new ArrayList<>();
		List<Long> paused = new ArrayList<>();
		response.setCreated(created).setKilled(killed).setUpdated(updated).setStarted(started);

		List<Long> existingSpiders = new ArrayList<>();
		containers.forEach(c -> {

			Spider containerSpider = c.spider();
			long containerSpiderId = containerSpider.getId();

			existingSpiders.add(containerSpiderId);
			if (!spiderByIdFromCoordinator.containsKey(containerSpiderId)) {
				log.debug("Killing spider {}", containerSpiderId);
				killCoordinatorContainer(containerSpiderId);
				killed.add(containerSpiderId);
			} else {
				Spider remoteSpider = spiderByIdFromCoordinator.get(containerSpiderId);

				if (remoteSpider.getVersion() != containerSpider.getVersion()) {
					log.debug("Updating spider {}", containerSpiderId);
					killCoordinatorContainer(containerSpiderId);
					create(remoteSpider);
					updated.add(containerSpiderId);

					if (SpiderStatuses.STARTED.equals(remoteSpider.getStatus())) {
						log.debug("Starting spider {}", containerSpiderId);
						startCoordinatorContainer(containerSpiderId);
						started.add(containerSpiderId);
					}
				} else if (!remoteSpider.getStatus().equalsIgnoreCase(c.status().toString())) {
					log.info("Container for {} is {}, but has to be {}", containerSpider.getId(), c.status(), remoteSpider.getStatus());

					switch (remoteSpider.getStatus()) {
					case SpiderStatuses.STARTED:
						if (!ContainerStatus.STARTED.equals(c.status())) {
							log.debug("Starting spider {}", containerSpiderId);
							startCoordinatorContainer(containerSpiderId);
							started.add(containerSpiderId);
						}
						break;
					case SpiderStatuses.INITIATED:
						if (!ContainerStatus.INITIATED.equals(c.status())) {
							log.debug("Re-init spider {}", containerSpiderId);
							killCoordinatorContainer(containerSpiderId);
							create(remoteSpider);
						}
						break;
					case SpiderStatuses.PAUSED:
						if (!ContainerStatus.PAUSED.equals(c.status())) {
							log.debug("Pausing spider {}", containerSpiderId);
							pauseCoordinatorContainer(containerSpiderId);
							paused.add(containerSpiderId);
						}
						break;
					}
				}
			}
		});

		spiderByIdFromCoordinator.forEach((id, spider) -> {
			if (!existingSpiders.contains(id)) {
				log.debug("Creating spider {}...", id);
				create(spider);
				created.add(spider.getId());

				if (SpiderStatuses.STARTED.equals(spider.getStatus())) {
					log.debug("Starting spider {}", id);
					startCoordinatorContainer(spider.getId());
					started.add(spider.getId());
				} else if (SpiderStatuses.PAUSED.equals(spider.getStatus())) {
					log.debug("Pausing spider {}", id);
					pauseCoordinatorContainer(spider.getId());
					paused.add(spider.getId());
				}
			}
		});
		return response;
	}

	@Override
	public void close() throws Exception {

	}
}
