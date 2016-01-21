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

import io.mandrel.common.NotFoundException;
import io.mandrel.common.container.ContainerStatus;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.SpiderStatuses;
import io.mandrel.common.sync.Container;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;
import io.mandrel.data.analysis.AnalysisService;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.endpoints.contracts.WorkerContract;
import io.mandrel.metrics.Accumulators;
import io.mandrel.transport.Clients;
import io.mandrel.worker.WorkerContainer;
import io.mandrel.worker.WorkerContainers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.weakref.jmx.internal.guava.base.Throwables;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class WorkerResource implements WorkerContract {

	@Autowired
	private Clients clients;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private AnalysisService analysisService;
	@Autowired
	private ExtractorService extractorService;
	@Autowired
	private Accumulators accumulators;

	private Supplier<? extends NotFoundException> workerNotFound = () -> new NotFoundException("Worker not found");

	@Override
	public void createWorkerContainer(byte[] definition) {
		Spider spider;
		try {
			spider = objectMapper.readValue(definition, Spider.class);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		create(spider);
	}

	public void create(Spider spider) {
		WorkerContainer container = new WorkerContainer(extractorService, accumulators, spider, clients);
		container.register();
	}

	@Override
	public void startWorkerContainer(Long id) {
		WorkerContainers.get(id).orElseThrow(workerNotFound).start();
	}

	@Override
	public void pauseWorkerContainer(Long id) {
		WorkerContainers.get(id).orElseThrow(workerNotFound).pause();
	}

	@Override
	public void killWorkerContainer(Long id) {
		WorkerContainers.get(id).orElseThrow(workerNotFound).kill();
	}

	@Override
	public List<Container> listRunningWorkerContainers() {
		return WorkerContainers.list().stream()
				.map(f -> new Container().setSpiderId(f.spider().getId()).setVersion(f.spider().getVersion()).setStatus(f.status()))
				.collect(Collectors.toList());
	}

	@Override
	public SyncResponse syncWorkers(SyncRequest sync) {
		Collection<? extends io.mandrel.common.container.Container> containers = WorkerContainers.list();
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
				killWorkerContainer(containerSpiderId);
				killed.add(containerSpiderId);
			} else {
				Spider remoteSpider = spiderByIdFromController.get(containerSpiderId);

				if (remoteSpider.getVersion() != containerSpider.getVersion()) {
					log.debug("Updating spider {}", containerSpiderId);
					killWorkerContainer(containerSpiderId);
					create(remoteSpider);
					updated.add(containerSpiderId);

					if (SpiderStatuses.STARTED.equals(remoteSpider.getStatus())) {
						log.debug("Starting spider {}", containerSpiderId);
						startWorkerContainer(containerSpiderId);
						started.add(containerSpiderId);
					}
				} else if (!remoteSpider.getStatus().equalsIgnoreCase(containerSpider.getStatus())) {
					log.info("Container for {} is {}, but has to be {}", containerSpider.getId(), c.status(), remoteSpider.getStatus());

					switch (remoteSpider.getStatus()) {
					case SpiderStatuses.STARTED:
						if (!ContainerStatus.STARTED.equals(c.status())) {
							log.debug("Starting spider {}", containerSpiderId);
							startWorkerContainer(containerSpiderId);
							started.add(containerSpiderId);
						}
						break;
					case SpiderStatuses.CREATED:
					case SpiderStatuses.PAUSED:
						if (!ContainerStatus.PAUSED.equals(c.status())) {
							log.debug("Pausing spider {}", containerSpiderId);
							pauseWorkerContainer(containerSpiderId);
							paused.add(containerSpiderId);
						}
						break;
					}
				}
			}
		});

		spiderByIdFromController.forEach((id, spider) -> {
			if (!existingSpiders.contains(id)) {
				log.debug("Creating spider {}", id);
				create(spider);
				created.add(spider.getId());

				if (SpiderStatuses.STARTED.equals(spider.getStatus())) {
					log.debug("Starting spider {}", id);
					startWorkerContainer(spider.getId());
					started.add(spider.getId());
				}
			}
		});
		return response;
	}

	@Override
	@SneakyThrows
	public byte[] analyse(Long id, String source) {
		Spider spider = WorkerContainers.get(id).orElseThrow(workerNotFound).spider();
		return objectMapper.writeValueAsBytes(analysisService.analyze(spider, source));
	}

	@Override
	public void close() throws Exception {

	}

}
