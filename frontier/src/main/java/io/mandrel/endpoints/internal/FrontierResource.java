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
import io.mandrel.common.net.Uri;
import io.mandrel.common.sync.Container;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;
import io.mandrel.endpoints.contracts.FrontierContract;
import io.mandrel.endpoints.contracts.Next;
import io.mandrel.frontier.FrontierContainer;
import io.mandrel.frontier.FrontierContainers;
import io.mandrel.metrics.Accumulators;
import io.mandrel.transport.Clients;
import io.mandrel.transport.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.weakref.jmx.internal.guava.base.Throwables;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

@Component
@Slf4j
public class FrontierResource implements FrontierContract {

	@Autowired
	private Accumulators accumulators;
	@Autowired
	private Clients clients;
	@Autowired
	private ObjectMapper objectMapper;

	private Supplier<? extends NotFoundException> frontierNotFound = () -> new NotFoundException("Frontier not found");

	@Override
	public void createFrontierContainer(byte[] definition) {
		Spider spider;
		try {
			spider = objectMapper.readValue(definition, Spider.class);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		create(spider);
	}

	public void create(Spider spider) {
		FrontierContainer container = new FrontierContainer(spider, accumulators, clients);
		container.register();
	}

	@Override
	public void startFrontierContainer(Long id) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).start();
	}

	@Override
	public void pauseFrontierContainer(Long id) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).pause();
	}

	@Override
	public void killFrontierContainer(Long id) {
		FrontierContainer container = FrontierContainers.get(id).orElseThrow(frontierNotFound);
		container.kill();
		container.unregister();
	}

	@Override
	public void schedule(Long id, Uri uri) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).frontier().schedule(uri);
	}

	@Override
	public void mschedule(Long id, Set<Uri> uris) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).frontier().schedule(uris);
	}

	@Override
	public void delete(Long id, Uri uri) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).frontier().schedule(uri);
	}

	@Override
	public List<Container> listRunningFrontierContainers() {
		return FrontierContainers.list().stream()
				.map(f -> new Container().setSpiderId(f.spider().getId()).setVersion(f.spider().getVersion()).setStatus(f.status()))
				.collect(Collectors.toList());
	}

	@Override
	public void close() throws Exception {

	}

	@Override
	public SyncResponse syncFrontiers(SyncRequest sync) {
		Collection<? extends io.mandrel.common.container.Container> containers = FrontierContainers.list();
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
				killFrontierContainer(containerSpiderId);
				killed.add(containerSpiderId);
			} else {
				Spider remoteSpider = spiderByIdFromController.get(containerSpiderId);

				if (remoteSpider.getVersion() != containerSpider.getVersion()) {
					log.debug("Updating spider {}", containerSpiderId);
					killFrontierContainer(containerSpiderId);
					create(remoteSpider);
					updated.add(containerSpiderId);

					if (SpiderStatuses.STARTED.equals(remoteSpider.getStatus())) {
						log.debug("Starting spider {}", containerSpiderId);
						startFrontierContainer(containerSpiderId);
						started.add(containerSpiderId);
					}
				} else if (!remoteSpider.getStatus().equalsIgnoreCase(c.status().toString())) {
					log.info("Container for {} is {}, but has to be {}", containerSpider.getId(), c.status(), remoteSpider.getStatus());

					switch (remoteSpider.getStatus()) {
					case SpiderStatuses.STARTED:
						if (!ContainerStatus.STARTED.equals(c.status())) {
							log.debug("Starting spider {}", containerSpiderId);
							startFrontierContainer(containerSpiderId);
							started.add(containerSpiderId);
						}
						break;
					case SpiderStatuses.CREATED:
						if (!ContainerStatus.INITIATED.equals(c.status())) {
							log.debug("Re-init spider {}", containerSpiderId);
							killFrontierContainer(containerSpiderId);
							create(remoteSpider);
						}
						break;
					case SpiderStatuses.PAUSED:
						if (!ContainerStatus.PAUSED.equals(c.status())) {
							log.debug("Pausing spider {}", containerSpiderId);
							pauseFrontierContainer(containerSpiderId);
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
					startFrontierContainer(spider.getId());
					started.add(spider.getId());
				} else if (SpiderStatuses.PAUSED.equals(spider.getStatus())) {
					log.debug("Pausing spider {}", id);
					pauseFrontierContainer(spider.getId());
					paused.add(spider.getId());
				}
			}
		});
		return response;
	}

	@Override
	public ListenableFuture<Next> next(Long id) {
		SettableFuture<Next> result = SettableFuture.create();
		FrontierContainers.get(id).orElseThrow(frontierNotFound).frontier().pool((uri, name) -> {
			result.set(new Next().setUri(uri).setFromStore(name));
		});
		try {
			return result;
		} catch (Exception e) {
			log.debug("Well...", e);
			throw RemoteException.of(RemoteException.Error.G_UNKNOWN, e.getMessage());
		}
	}
}
