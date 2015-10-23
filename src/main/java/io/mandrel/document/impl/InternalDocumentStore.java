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
package io.mandrel.document.impl;

import io.mandrel.common.data.Header;
import io.mandrel.common.data.HttpStrategy;
import io.mandrel.common.data.Param;
import io.mandrel.common.data.HttpStrategy.HttpStrategyDefinition;
import io.mandrel.common.service.TaskContext;
import io.mandrel.data.content.MetadataExtractor;
import io.mandrel.document.Document;
import io.mandrel.document.DocumentStore;
import io.mandrel.requests.http.Cookie;
import io.mandrel.requests.http.ua.UserAgentProvisionner.UserAgentProvisionnerDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.util.IterationType;

public class InternalDocumentStore extends DocumentStore {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class InternalDocumentStoreDefinition implements DocumentStoreDefinition {

		private static final long serialVersionUID = -9205125497698919267L;

		@Override
		public String name() {
			return "internal";
		}

		@Override
		public InternalDocumentStore build(TaskContext context) {
			return new InternalDocumentStore(context);
		}
	}

	protected final MetadataExtractor extractor;

	private final HazelcastInstance hazelcastInstance;

	public InternalDocumentStore(TaskContext context) {
		super(context);
		hazelcastInstance = context.getInstance();
		extractor = null;// TODO ???
	}

	@Override
	public void save(Document data) {
		if (data != null) {
			getDataMap().put(getKey(data), data);
		}
	}

	@Override
	public void save(List<Document> data) {
		if (data != null) {
			data.forEach(el -> {
				getDataMap().put(getKey(el), el);
			});
		}
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
	public void deleteAll() {
		getDataMap().clear();
	}

	@Override
	public void byPages(int pageSize, Callback callback) {
		PagingPredicate predicate = new PagingPredicate(pageSize);
		predicate.setIterationType(IterationType.VALUE);

		boolean loop = true;
		while (loop) {
			Collection<Document> values = hazelcastInstance.<String, Document> getMap("documentstore-" + context.getSpiderId() + "-" + extractor.getName())
					.values(predicate);
			loop = callback.on(values);
			predicate.nextPage();
		}
	}

	@Override
	public long total() {
		return hazelcastInstance.<String, Document> getMap("documentstore-" + context.getSpiderId() + "-" + extractor.getName()).size();
	}

	@Override
	public Collection<Document> byPages(int pageSize, int pageNumber) {
		PagingPredicate predicate = new PagingPredicate(pageSize);
		predicate.setIterationType(IterationType.VALUE);
		IntStream.range(0, pageNumber).forEach(i -> predicate.nextPage());
		return hazelcastInstance.<String, Document> getMap("documentstore-" + context.getSpiderId() + "-" + extractor.getName()).values(predicate);
	}

	public String getKey(Document data) {
		String key = null;
		if (StringUtils.isNotBlank(extractor.getKeyField())) {
			List<? extends Object> values = data.get(extractor.getKeyField());
			if (CollectionUtils.isNotEmpty(values)) {
				key = values.get(0).toString();
			}
		}
		if (key == null) {
			key = String.valueOf(hazelcastInstance.getIdGenerator("documentstore-" + context.getSpiderId() + "-" + extractor.getName()).newId());
		}
		return key;
	}

	public IMap<String, Document> getDataMap() {
		return hazelcastInstance.getMap("documentstore-" + context.getSpiderId() + "-" + extractor.getName());
	}

	@Override
	public void init() {
	}
}
