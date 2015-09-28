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

import io.mandrel.data.content.MetadataExtractor;
import io.mandrel.gateway.Document;
import io.mandrel.gateway.DocumentStore;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.util.IterationType;

@Data
public class InternalDocumentStore implements DocumentStore {

	private static final long serialVersionUID = -2445958974306201476L;

	@JsonIgnore
	protected MetadataExtractor extractor;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	protected transient HazelcastInstance hazelcastInstance;

	@Override
	public void save(long spiderId, Document data) {
		if (data != null) {
			getDataMap(spiderId).put(getKey(spiderId, data), data);
		}
	}

	@Override
	public void save(long spiderId, List<Document> data) {
		if (data != null) {
			data.forEach(el -> {
				getDataMap(spiderId).put(getKey(spiderId, el), el);
			});
		}
	}

	@Override
	public void init(MetadataExtractor webPageExtractor) {
		this.extractor = webPageExtractor;
	}

	@Override
	public boolean check() {
		try {
			hazelcastInstance.getCluster();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void deleteAllFor(long spiderId) {
		getDataMap(spiderId).clear();
	}

	@Override
	public void byPages(long spiderId, int pageSize, Callback callback) {
		PagingPredicate predicate = new PagingPredicate(pageSize);
		predicate.setIterationType(IterationType.VALUE);

		boolean loop = true;
		while (loop) {
			Collection<Document> values = hazelcastInstance.<String, Document> getMap("documentstore-" + spiderId + "-" + extractor.getName())
					.values(predicate);
			loop = callback.on(values);
			predicate.nextPage();
		}
	}

	@Override
	public long total(long spiderId) {
		return hazelcastInstance.<String, Document> getMap("documentstore-" + spiderId + "-" + extractor.getName()).size();
	}

	@Override
	public Collection<Document> byPages(long spiderId, int pageSize, int pageNumber) {
		PagingPredicate predicate = new PagingPredicate(pageSize);
		predicate.setIterationType(IterationType.VALUE);
		IntStream.range(0, pageNumber).forEach(i -> predicate.nextPage());
		return hazelcastInstance.<String, Document> getMap("documentstore-" + spiderId + "-" + extractor.getName()).values(predicate);
	}

	public String getKey(long spiderId, Document data) {
		String key = null;
		if (StringUtils.isNotBlank(extractor.getKeyField())) {
			List<? extends Object> values = data.get(extractor.getKeyField());
			if (CollectionUtils.isNotEmpty(values)) {
				key = values.get(0).toString();
			}
		}
		if (key == null) {
			key = String.valueOf(hazelcastInstance.getIdGenerator("documentstore-" + spiderId + "-" + extractor.getName()).newId());
		}
		return key;
	}

	public IMap<String, Document> getDataMap(long spiderId) {
		return hazelcastInstance.getMap("documentstore-" + spiderId + "-" + extractor.getName());
	}

	@Override
	public String getType() {
		return "internal";
	}
}
