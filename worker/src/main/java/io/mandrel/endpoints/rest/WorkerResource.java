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

import io.mandrel.common.client.Clients;
import io.mandrel.common.data.Spider;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.endpoints.contracts.WorkerContract;
import io.mandrel.metrics.Accumulators;
import io.mandrel.worker.WorkerContainer;
import io.mandrel.worker.WorkerContainers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RestController;

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

	@Override
	public void create(Spider spider, URI target) {
		WorkerContainer container = new WorkerContainer(extractorService, accumulators, spider, clients, discoveryClient);
		container.register();
	}

	@Override
	public void start(Long id, URI target) {
		WorkerContainers.get(id).ifPresent(c -> c.start());
	}

	@Override
	public void pause(Long id, URI target) {
		WorkerContainers.get(id).ifPresent(c -> c.pause());
	}

	@Override
	public void kill(Long id, URI target) {
		WorkerContainers.get(id).ifPresent(c -> c.kill());
	}

	@Override
	public Map<Long, Long> listActive(URI target) {
		return WorkerContainers.list().stream().map(w -> w.spider().getId()).collect(Collectors.toMap(i -> i, i -> i));
	}

	@Override
	public void sync(List<Spider> spiders, URI target) {
		Map<Long, Spider> ids = spiders.stream().collect(Collectors.toMap(spider -> spider.getId(), spider -> spider));

		List<Long> existingSpiders = new ArrayList<>();
		WorkerContainers.list().forEach(c -> {
			existingSpiders.add(c.spider().getId());
			if (!ids.containsKey(c.spider().getId())) {
				kill(c.spider().getId(), null);
			} else {
				if (ids.get(c.spider().getId()).getVersion() != c.spider().getVersion()) {
					kill(c.spider().getId(), null);
					create(ids.get(c.spider().getId()), null);
				}
			}
		});

		ids.forEach((id, spider) -> {
			if (!existingSpiders.contains(id)) {
				create(spider, null);
			}
		});
	}
}
