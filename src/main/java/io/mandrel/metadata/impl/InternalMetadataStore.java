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
package io.mandrel.metadata.impl;

import io.mandrel.common.service.TaskContext;
import io.mandrel.data.Link;
import io.mandrel.frontier.Politeness;
import io.mandrel.metadata.FetchMetadata;
import io.mandrel.metadata.MetadataStore;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class InternalMetadataStore extends MetadataStore {

	@Data
	public static class InternalMetadataStoreDefinition implements MetadataStoreDefinition {

		private static final long serialVersionUID = -9205125497698919267L;

		@Override
		public String name() {
			return "internal";
		}

		@Override
		public MetadataStore build(TaskContext context) {
			return new InternalMetadataStore(context);
		}
	}

	private final HazelcastInstance hazelcastInstance;

	public InternalMetadataStore(TaskContext context) {
		super(context);
		hazelcastInstance = context.getInstance();
	}

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void addMetadata(URI uri, FetchMetadata webPage) {
		getMetadata().put(uri, webPage);
	}

	@Override
	public Set<Link> filter(Set<Link> outlinks, Politeness politeness) {

		if (outlinks == null) {
			return null;
		}

		Set<URI> uris = outlinks.stream().filter(ol -> ol != null).map(ol -> ol.uri()).collect(Collectors.toSet());
		Map<URI, FetchMetadata> all = getMetadata().getAll(uris);

		int recrawlAfterSeconds = politeness.getRecrawlAfterSeconds();
		LocalDateTime now = LocalDateTime.now();
		return outlinks.stream().filter(outlink -> {
			FetchMetadata entry = all.get(outlink.uri());

			if (entry == null) {
				return true;
			}

			if (recrawlAfterSeconds > 0 && now.minusSeconds(recrawlAfterSeconds).isAfter(entry.lastCrawlDate())) {
				return true;
			}

			return false;
		}).collect(Collectors.toSet());
	}

	@Override
	public FetchMetadata getMetadata(URI uri) {
		return getMetadata().get(uri);
	}

	@Override
	public void deleteAll() {
		getMetadata().destroy();
	}

	public IMap<URI, FetchMetadata> getMetadata() {
		return hazelcastInstance.<URI, FetchMetadata> getMap("metadata-" + context.getSpiderId());
	}

	@Override
	public void init() {
		
	}
}
