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
package io.mandrel.endpoints;

import io.mandrel.common.NotFoundException;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Statuses;
import io.mandrel.common.net.Uri;
import io.mandrel.common.sync.Container;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;
import io.mandrel.common.thrift.Clients;
import io.mandrel.endpoints.contracts.FrontierContract;
import io.mandrel.frontier.FrontierContainer;
import io.mandrel.frontier.FrontierContainers;
import io.mandrel.metrics.Accumulators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.weakref.jmx.internal.guava.base.Throwables;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

@RestController
public class FrontierResource implements FrontierContract {

	@Autowired
	private Accumulators accumulators;
	@Autowired
	private Clients clients;
	@Autowired
	private ObjectMapper objectMapper;

	private Supplier<? extends NotFoundException> frontierNotFound = () -> new NotFoundException("Frontier not found");

	@Override
	public void create(byte[] definition) {
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
	public void start(Long id) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).start();
	}

	@Override
	public void pause(Long id) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).pause();
	}

	@Override
	public void kill(Long id) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).kill();
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
	public List<Container> listRunningContainers() {
		return FrontierContainers.list().stream()
				.map(f -> new Container().setSpiderId(f.spider().getId()).setVersion(f.spider().getVersion()).setStatus(f.status()))
				.collect(Collectors.toList());
	}

	@Override
	public void close() throws Exception {

	}

	@Override
	public SyncResponse sync(SyncRequest sync) {
		Collection<? extends io.mandrel.common.container.Container> containers = FrontierContainers.list();
		Map<Long, Spider> ids = sync.getDefinitions().stream().map(def -> {
			try {
				return objectMapper.readValue(def, Spider.class);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}).collect(Collectors.toMap(spider -> spider.getId(), spider -> spider));

		SyncResponse response = new SyncResponse();
		List<Long> created = new ArrayList<>();
		List<Long> updated = new ArrayList<>();
		List<Long> deleted = new ArrayList<>();
		response.setCreated(created).setDeleted(deleted).setUpdated(updated);
		List<Long> existingSpiders = new ArrayList<>();
		containers.forEach(c -> {
			existingSpiders.add(c.spider().getId());
			if (!ids.containsKey(c.spider().getId())) {
				kill(c.spider().getId());
				deleted.add(c.spider().getId());
			} else if (ids.get(c.spider().getId()).getVersion() != c.spider().getVersion()) {
				kill(c.spider().getId());
				create(ids.get(c.spider().getId()));
				updated.add(c.spider().getId());

				if (Statuses.STARTED.equals(ids.get(c.spider().getId()).getStatus())) {
					start(c.spider().getId());
				}
			}
		});

		ids.forEach((id, spider) -> {
			if (!existingSpiders.contains(id)) {
				create(spider);

				if (Statuses.STARTED.equals(spider.getStatus())) {
					start(spider.getId());
				}
				created.add(spider.getId());
			}
		});
		return response;
	}

	@Override
	public ListenableFuture<Uri> next(Long id) {
		SettableFuture<Uri> result = SettableFuture.create();
		FrontierContainers.get(id).orElseThrow(frontierNotFound).frontier().pool(uri -> {
			result.set(uri);
		});
		return result;
	}
}
