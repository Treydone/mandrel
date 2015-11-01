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

import io.mandrel.common.data.Spider;
import io.mandrel.endpoints.contracts.FrontierContract;
import io.mandrel.frontier.Frontier;
import io.mandrel.frontier.FrontierContainer;
import io.mandrel.frontier.FrontierContainers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FrontierResource implements FrontierContract {

	@Override
	public void create(Spider spider, URI target) {
		FrontierContainer container = new FrontierContainer(spider);
		container.register();
	}

	@Override
	public void start(Long id, URI target) {
		FrontierContainers.get(id).ifPresent(c -> c.start());
	}

	@Override
	public void pause(Long id, URI target) {
		FrontierContainers.get(id).ifPresent(c -> c.pause());
	}

	@Override
	public void kill(Long id, URI target) {
		FrontierContainers.get(id).ifPresent(c -> c.kill());
	}

	@Override
	public Optional<Frontier> id(Long id, URI target) {
		return FrontierContainers.get(id).map(c -> c.frontier());
	}

	@Override
	public URI next(Long id, URI target) {
		URI pool = FrontierContainers.get(id).map(f -> f.frontier().pool()).orElse(null);
		return pool;
	}

	@Override
	public void schedule(Long id, URI uri, URI target) {
		FrontierContainers.get(id).ifPresent(f -> f.frontier().schedule(uri));
	}

	@Override
	public void schedule(Long id, Set<URI> uris, URI target) {
		FrontierContainers.get(id).ifPresent(f -> f.frontier().schedule(uris));
	}

	@Override
	public void delete(Long id, URI uri, URI target) {
		FrontierContainers.get(id).ifPresent(f -> f.frontier().schedule(uri));
	}

	@Override
	public Map<Long, Long> listActive(URI target) {
		return FrontierContainers.list().stream().map(f -> f.spider().getId()).collect(Collectors.toMap(i -> i, i -> i));
	}

	@Override
	public void sync(List<Spider> spiders, URI target) {
		Map<Long, Spider> ids = spiders.stream().collect(Collectors.toMap(spider -> spider.getId(), spider -> spider));

		List<Long> existingSpiders = new ArrayList<>();
		FrontierContainers.list().forEach(c -> {
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
