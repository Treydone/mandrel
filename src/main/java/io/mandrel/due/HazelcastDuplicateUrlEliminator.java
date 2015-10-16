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
package io.mandrel.due;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HazelcastDuplicateUrlEliminator implements DuplicateUrlEliminator {

	private final HazelcastInstance instance;

	public void markAsPending(String queueName, URI uri) {
		if (uri != null) {
			prepareIfNotDefined(queueName);

			IMap<URI, Boolean> pendings = instance.getMap(queueName);
			pendings.put(uri, Boolean.TRUE);
		}
	}

	public void removePending(String queueName, URI uri) {
		if (uri != null) {
			prepareIfNotDefined(queueName);

			IMap<String, URI> pendings = instance.getMap(queueName);
			pendings.remove(uri);
		}
	}

	public Set<URI> filterPendings(String queueName, Collection<URI> uris) {
		if (uris != null) {
			prepareIfNotDefined(queueName);

			IMap<String, URI> pendings = instance.getMap(queueName);
			return uris.stream().filter(el -> !pendings.containsKey(el)).collect(Collectors.toSet());
		}
		return null;
	}

	protected void prepareIfNotDefined(String queueName) {
		if (instance.getConfig().getQueueConfigs().containsKey(queueName)) {
			// Create map of pendings with TTL of 10 secs
			MapConfig mapConfig = new MapConfig();
			mapConfig.setName(queueName).setBackupCount(1).setTimeToLiveSeconds(10).setStatisticsEnabled(true);
			instance.getConfig().addMapConfig(mapConfig);
		}
	}

	public Set<URI> deduplicate(String queueName, Collection<URI> uris) {
		if (uris != null) {
			IQueue<URI> queue = instance.getQueue(queueName);
			return uris.stream().filter(el -> !queue.contains(el)).collect(Collectors.toSet());
		}
		return null;
	}
}
