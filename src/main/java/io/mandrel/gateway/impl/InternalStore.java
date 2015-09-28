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
package io.mandrel.gateway.impl;

import io.mandrel.common.data.Politeness;
import io.mandrel.data.spider.Link;
import io.mandrel.gateway.BlobStore;
import io.mandrel.gateway.MetadataStore;
import io.mandrel.requests.Bag;
import io.mandrel.requests.Metadata;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.util.IterationType;

@Data
public class InternalStore implements BlobStore, MetadataStore {

	private static final long serialVersionUID = -775049235484042261L;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	protected transient HazelcastInstance hazelcastInstance;

	public InternalStore() {
	}

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void init(Map<String, Object> properties) {

	}

	@Override
	public void addMetadata(long spiderId, String url, Metadata webPage) {
		getPageMetaMap(spiderId).put(url, webPage);
	}

	@Override
	public void addBag(long spiderId, String url, Bag<? extends Metadata> bag) {
		getPageMap(spiderId).set(url, bag);
	}

	@Override
	public Bag<? extends Metadata> getBag(long spiderId, String url) {
		return getPageMap(spiderId).get(url);
	}

	@Override
	public Set<String> filter(long spiderId, Set<Link> outlinks, Politeness politeness) {

		if (outlinks == null) {
			return null;
		}

		Set<String> uris = outlinks.stream().filter(ol -> ol != null && StringUtils.isNotBlank(ol.getUri())).map(ol -> ol.getUri()).collect(Collectors.toSet());
		Map<String, Metadata> all = getPageMetaMap(spiderId).getAll(uris);

		int recrawlAfterSeconds = politeness.getRecrawlAfterSeconds();
		LocalDateTime now = LocalDateTime.now();
		return outlinks.stream().filter(outlink -> {
			Metadata entry = all.get(outlink.getUri());

			if (entry == null) {
				return true;
			}

			if (recrawlAfterSeconds > 0 && now.minusSeconds(recrawlAfterSeconds).isAfter(entry.getLastCrawlDate())) {
				return true;
			}

			return false;
		}).map(l -> l.getUri()).collect(Collectors.toSet());
	}

	@Override
	public void deleteAllFor(long spiderId) {
		getPageMap(spiderId).destroy();
		getPageMetaMap(spiderId).destroy();
	}

	@Override
	public void byPages(long spiderId, int pageSize, Callback callback) {
		PagingPredicate predicate = new PagingPredicate(pageSize);
		predicate.setIterationType(IterationType.VALUE);

		boolean loop = true;
		while (loop) {
			Collection<Bag<? extends Metadata>> values = getPageMap(spiderId).values(predicate);
			loop = callback.on(values);
			predicate.nextPage();
		}
	}

	@Override
	public Metadata getMetadata(long spiderId, String url) {
		return getPageMetaMap(spiderId).get(url);
	}

	public IMap<String, Bag<? extends Metadata>> getPageMap(long spiderId) {
		return hazelcastInstance.<String, Bag<? extends Metadata>> getMap("blob-" + spiderId);
	}

	public IMap<String, Metadata> getPageMetaMap(long spiderId) {
		return hazelcastInstance.<String, Metadata> getMap("metadata-" + spiderId);
	}

	@Override
	public String getType() {
		return "internal";
	}
}
