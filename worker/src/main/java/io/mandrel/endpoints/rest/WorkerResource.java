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
package io.mandrel.endpoints.rest;

import io.mandrel.common.NotFoundException;
import io.mandrel.common.client.Clients;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Statuses;
import io.mandrel.common.sync.Container;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.endpoints.contracts.WorkerContract;
import io.mandrel.metrics.Accumulators;
import io.mandrel.worker.WorkerContainer;
import io.mandrel.worker.WorkerContainers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class WorkerResource implements WorkerContract {

	@Autowired
	private ExtractorService extractorService;
	@Autowired
	private Accumulators accumulators;
	@Autowired
	private Clients clients;
	@Autowired
	private DiscoveryClient discoveryClient;

	private Supplier<? extends NotFoundException> workerNotFound = () -> new NotFoundException("Worker not found");

	@Override
	public void create(Spider spider, URI target) {
		WorkerContainer container = new WorkerContainer(extractorService, accumulators, spider, clients, discoveryClient);
		container.register();
	}

	@Override
	public void start(Long id, URI target) {
		WorkerContainers.get(id).orElseThrow(workerNotFound).start();
	}

	@Override
	public void pause(Long id, URI target) {
		WorkerContainers.get(id).orElseThrow(workerNotFound).pause();
	}

	@Override
	public void kill(Long id, URI target) {
		WorkerContainers.get(id).orElseThrow(workerNotFound).kill();
	}

	@Override
	public List<Container> listContainers(URI target) {
		return WorkerContainers.list().stream()
				.map(f -> new Container().setSpiderId(f.spider().getId()).setVersion(f.spider().getVersion()).setStatus(f.status()))
				.collect(Collectors.toList());
	}

	@Override
	public SyncResponse sync(@RequestBody SyncRequest sync, URI target) {
		Collection<? extends io.mandrel.common.container.Container> containers = WorkerContainers.list();
		Map<Long, Spider> ids = sync.getSpiders().stream().collect(Collectors.toMap(spider -> spider.getId(), spider -> spider));

		SyncResponse response = new SyncResponse();
		List<Long> created = new ArrayList<>();
		List<Long> updated = new ArrayList<>();
		List<Long> deleted = new ArrayList<>();
		response.setCreated(created).setDeleted(deleted).setUpdated(updated);

		List<Long> existingSpiders = new ArrayList<>();
		containers.forEach(c -> {
			existingSpiders.add(c.spider().getId());
			if (!ids.containsKey(c.spider().getId())) {
				log.debug("Killing spider {}", c.spider().getId());
				kill(c.spider().getId(), null);
				deleted.add(c.spider().getId());
			} else if (ids.get(c.spider().getId()).getVersion() != c.spider().getVersion()) {
				log.debug("Updating spider {}", c.spider().getId());
				kill(c.spider().getId(), null);
				create(ids.get(c.spider().getId()), null);
				updated.add(c.spider().getId());

				if (Statuses.STARTED.equals(ids.get(c.spider().getId()).getStatus())) {
					log.debug("Starting spider {}", c.spider().getId());
					start(c.spider().getId(), null);
				}
			}
		});

		ids.forEach((id, spider) -> {
			if (!existingSpiders.contains(id)) {
				log.debug("Creating spider {}", id);
				create(spider, null);

				if (Statuses.STARTED.equals(spider.getStatus())) {
					log.debug("Starting spider {}", id);
					start(spider.getId(), null);
				}
				created.add(spider.getId());
			}
		});
		return response;
	}
}
