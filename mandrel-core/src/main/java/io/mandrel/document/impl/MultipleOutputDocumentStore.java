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

import io.mandrel.common.service.TaskContext;
import io.mandrel.data.content.DataExtractor;
import io.mandrel.document.Document;
import io.mandrel.document.DocumentStore;
import io.mandrel.document.NavigableDocumentStore;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true, fluent = true)
public class MultipleOutputDocumentStore extends NavigableDocumentStore {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class MultipleOutputDocumentStoreDefinition extends DocumentStoreDefinition<MultipleOutputDocumentStore> {

		private static final long serialVersionUID = -9108353520012009709L;

		@JsonProperty("stores")
		private List<? extends DocumentStoreDefinition<? extends DocumentStore>> stores;

		@Override
		public String name() {
			return "multiple";
		}

		@Override
		public MultipleOutputDocumentStore build(TaskContext context) {
			List<? extends DocumentStore> documentStores = stores.stream().map(definition -> definition.build(context)).collect(Collectors.toList());
			return new MultipleOutputDocumentStore(context, dataExtractor, documentStores);
		}
	}

	private final List<? extends DocumentStore> stores;

	public MultipleOutputDocumentStore(TaskContext context, DataExtractor metadataExtractor, List<? extends DocumentStore> stores) {
		super(context, metadataExtractor);
		this.stores = stores;
	}

	@Override
	public void save(Document data) {
		if (data != null) {
			stores.forEach(store -> store.save(data));
		}
	}

	@Override
	public void save(List<Document> data) {
		if (data != null) {
			stores.forEach(store -> store.save(data));
		}
	}

	@Override
	public boolean check() {
		// TODO
		return true;
	}

	@Override
	public void deleteAll() {
		stores.forEach(store -> {
			if (store.isNavigable()) {
				((NavigableDocumentStore) store).deleteAll();
			}
		});
	}

	@Override
	public void byPages(int pageSize, Callback callback) {
		((NavigableDocumentStore) stores.get(0)).byPages(pageSize, callback);
	}

	@Override
	public long total() {
		return ((NavigableDocumentStore) stores.get(0)).total();
	}

	@Override
	public Collection<Document> byPages(int pageSize, int pageNumber) {
		return ((NavigableDocumentStore) stores.get(0)).byPages(pageSize, pageNumber);
	}

	@Override
	public void init() {
	}

	@Override
	public void close() throws IOException {
		stores.forEach(store -> {
			try {
				store.close();
			} catch (Exception e) {
				log.warn("Well...", e);
			}
		});
	}
}
