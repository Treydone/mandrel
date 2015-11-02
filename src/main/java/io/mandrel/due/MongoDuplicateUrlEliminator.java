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

import io.mandrel.common.data.Header;
import io.mandrel.common.data.HttpStrategy;
import io.mandrel.common.data.Param;
import io.mandrel.common.data.HttpStrategy.HttpStrategyDefinition;
import io.mandrel.common.service.TaskContext;
import io.mandrel.requests.http.Cookie;
import io.mandrel.requests.http.ua.UserAgentProvisionner.UserAgentProvisionnerDefinition;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

public class MongoDuplicateUrlEliminator extends DuplicateUrlEliminator {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class MongoDuplicateUrlEliminatorDefinition implements DuplicateUrlEliminatorDefinition {

		private static final long serialVersionUID = -9205125497698919267L;

		@Override
		public String name() {
			return "mongo";
		}

		@Override
		public MongoDuplicateUrlEliminator build(TaskContext context) {
			return new MongoDuplicateUrlEliminator(context);
		}
	}

	public MongoDuplicateUrlEliminator(TaskContext context) {
		super(context);
	}

	public void markAsPending(URI uri) {
		if (uri != null) {
			prepareIfNotDefined("pendings-" + context.getSpiderId());

			IMap<URI, Boolean> pendings = getPendings();
			pendings.put(uri, Boolean.TRUE);
		}
	}

	public IMap<URI, Boolean> getPendings() {
		return hazelcastInstance.getMap("pendings-" + context.getSpiderId());
	}

	public void removePending(URI uri) {
		if (uri != null) {
			prepareIfNotDefined("pendings-" + context.getSpiderId());

			IMap<URI, Boolean> pendings = getPendings();
			pendings.remove(uri);
		}
	}

	public Set<URI> filterPendings(Collection<URI> uris) {
		if (uris != null) {
			prepareIfNotDefined("pendings-" + context.getSpiderId());

			IMap<URI, Boolean> pendings = getPendings();
			return uris.stream().filter(el -> !pendings.containsKey(el)).collect(Collectors.toSet());
		}
		return null;
	}

	public Set<URI> deduplicate(Collection<URI> uris) {
		if (uris != null) {
			IQueue<URI> queue = hazelcastInstance.getQueue("pendings-" + context.getSpiderId());
			return uris.stream().filter(el -> !queue.contains(el)).collect(Collectors.toSet());
		}
		return null;
	}

	@Override
	public boolean check() {
		return true;
	}
}
