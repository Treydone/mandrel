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

import io.mandrel.cluster.idgenerator.IdGenerator;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.State;

import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ControllerRepository {

	private final IdGenerator idGenerator;

	private final HazelcastInstance instance;

	public Spider add(Spider spider) {
		long id = idGenerator.generateId("spiders");
		spider.setId(id);
		spiders(instance).put(id, spider);
		return spider;
	}

	public Spider update(Spider spider) {
		spiders(instance).put(spider.getId(), spider);
		return spider;
	}

	public void delete(long id) {
		spiders(instance).remove(id);
	}

	public Optional<Spider> get(long id) {
		Spider value = spiders(instance).get(id);
		return value == null ? Optional.empty() : Optional.of(value);
	}

	public Stream<Spider> list() {
		return spiders(instance).values().stream().map(el -> el);
	}

	public Stream<Spider> listActive() {
		return list().filter(s -> s.getState().equals(State.STARTED));
	}

	// ------------------------------ TOOLS
	static ReplicatedMap<Long, Spider> spiders(HazelcastInstance instance) {
		return instance.getReplicatedMap("spiders");
	}
}
