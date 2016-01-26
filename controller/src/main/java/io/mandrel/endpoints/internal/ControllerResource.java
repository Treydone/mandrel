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
import io.mandrel.controller.ControllerContainer;
import io.mandrel.controller.ControllerContainers;
import io.mandrel.endpoints.contracts.ControllerContract;
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
public class ControllerResource implements ControllerContract {

	@Autowired
	private TimelineService timelineService;
	@Autowired
	private MetricsService metricsService;
	@Autowired
	private NodeRepository nodeRepository;
	@Autowired
	private ObjectMapper objectMapper;

	private Supplier<? extends NotFoundException> controllerNotFound = () -> new NotFoundException("Controller not found");

	@Override
	public void addEvent(Event event) {
		timelineService.add(event);
	}

	@Override
	public void updateMetrics(Map<String, Long> accumulators) {
		metricsService.sync(accumulators);
	}

	@Override
	public void createControllerContainer(byte[] definition) {
		Spider spider;
		try {
			spider = objectMapper.readValue(definition, Spider.class);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		create(spider);
	}

	public void create(Spider spider) {
		ControllerContainer container = new ControllerContainer(spider, null, null);
		container.register();
	}

	@Override
	public void startControllerContainer(Long id) {
		ControllerContainers.get(id).orElseThrow(controllerNotFound).start();
	}

	@Override
	public void pauseControllerContainer(Long id) {
		ControllerContainers.get(id).orElseThrow(controllerNotFound).pause();
	}

	@Override
	public void killControllerContainer(Long id) {
		ControllerContainer container = ControllerContainers.get(id).orElseThrow(controllerNotFound);
		container.kill();
		container.unregister();
	}

	@Override
	public List<Container> listRunningControllerContainers() {
		return ControllerContainers.list().stream()
				.map(f -> new Container().setSpiderId(f.spider().getId()).setVersion(f.spider().getVersion()).setStatus(f.status()))
				.collect(Collectors.toList());
	}

	@Override
	public SyncResponse syncControllers(SyncRequest sync) throws RemoteException {
		Collection<? extends io.mandrel.common.container.Container> containers = ControllerContainers.list();
		final Map<Long, Spider> spiderByIdFromController = new HashMap<>();
		if (sync.getDefinitions() != null) {
			spiderByIdFromController.putAll(sync.getDefinitions().stream().map(def -> {
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
			if (!spiderByIdFromController.containsKey(containerSpiderId)) {
				log.debug("Killing spider {}", containerSpiderId);
				killControllerContainer(containerSpiderId);
				killed.add(containerSpiderId);
			} else {
				Spider remoteSpider = spiderByIdFromController.get(containerSpiderId);

				if (remoteSpider.getVersion() != containerSpider.getVersion()) {
					log.debug("Updating spider {}", containerSpiderId);
					killControllerContainer(containerSpiderId);
					create(remoteSpider);
					updated.add(containerSpiderId);

					if (SpiderStatuses.STARTED.equals(remoteSpider.getStatus())) {
						log.debug("Starting spider {}", containerSpiderId);
						startControllerContainer(containerSpiderId);
						started.add(containerSpiderId);
					}
				} else if (!remoteSpider.getStatus().equalsIgnoreCase(c.status().toString())) {
					log.info("Container for {} is {}, but has to be {}", containerSpider.getId(), c.status(), remoteSpider.getStatus());

					switch (remoteSpider.getStatus()) {
					case SpiderStatuses.STARTED:
						if (!ContainerStatus.STARTED.equals(c.status())) {
							log.debug("Starting spider {}", containerSpiderId);
							startControllerContainer(containerSpiderId);
							started.add(containerSpiderId);
						}
						break;
					case SpiderStatuses.CREATED:
						if (!ContainerStatus.INITIATED.equals(c.status())) {
							log.debug("Re-init spider {}", containerSpiderId);
							killControllerContainer(containerSpiderId);
							create(remoteSpider);
						}
						break;
					case SpiderStatuses.PAUSED:
						if (!ContainerStatus.PAUSED.equals(c.status())) {
							log.debug("Pausing spider {}", containerSpiderId);
							pauseControllerContainer(containerSpiderId);
							paused.add(containerSpiderId);
						}
						break;
					}
				}
			}
		});

		spiderByIdFromController.forEach((id, spider) -> {
			if (!existingSpiders.contains(id)) {
				log.debug("Creating spider {}...", id);
				create(spider);
				created.add(spider.getId());

				if (SpiderStatuses.STARTED.equals(spider.getStatus())) {
					log.debug("Starting spider {}", id);
					startControllerContainer(spider.getId());
					started.add(spider.getId());
				} else if (SpiderStatuses.PAUSED.equals(spider.getStatus())) {
					log.debug("Pausing spider {}", id);
					pauseControllerContainer(spider.getId());
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
