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

import io.mandrel.data.spider.Link;
import io.mandrel.frontier.Politeness;
import io.mandrel.metadata.FetchMetadata;
import io.mandrel.metadata.MetadataStore;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Data
public class MetadataInternalStore implements MetadataStore {

	private static final long serialVersionUID = -775049235484042261L;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	protected transient HazelcastInstance hazelcastInstance;

	public MetadataInternalStore() {
	}

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void init(Map<String, Object> properties) {

	}

	@Override
	public void addMetadata(long spiderId, URI uri, FetchMetadata webPage) {
		getMetadata(spiderId).put(uri, webPage);
	}

	@Override
	public Set<Link> filter(long spiderId, Set<Link> outlinks, Politeness politeness) {

		if (outlinks == null) {
			return null;
		}

		Set<URI> uris = outlinks.stream().filter(ol -> ol != null).map(ol -> ol.uri()).collect(Collectors.toSet());
		Map<URI, FetchMetadata> all = getMetadata(spiderId).getAll(uris);

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
	public FetchMetadata getMetadata(long spiderId, URI uri) {
		return getMetadata(spiderId).get(uri);
	}

	@Override
	public void deleteAllFor(long spiderId) {
		getMetadata(spiderId).destroy();
	}

	public IMap<URI, FetchMetadata> getMetadata(long spiderId) {
		return hazelcastInstance.<URI, FetchMetadata> getMap("metadata-" + spiderId);
	}

	@Override
	public String name() {
		return "internal";
	}
}
