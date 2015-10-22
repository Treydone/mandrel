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
package io.mandrel.frontier;

import io.mandrel.blob.BlobStores;
import io.mandrel.common.container.Container;
import io.mandrel.common.data.Spider;
import io.mandrel.common.service.TaskContext;
import io.mandrel.document.DocumentStores;
import io.mandrel.metadata.MetadataStores;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import com.hazelcast.core.HazelcastInstance;

@Data
@RequiredArgsConstructor
public class FrontierContainer implements Container {

	private final Spider spider;
	private final HazelcastInstance instance;

	@Override
	public String type() {
		return "frontier";
	}

	@Override
	public void start() {

		// Create context
		TaskContext context = new TaskContext();
		context.setDefinition(spider);
		context.setInstance(instance);

		// Init stores
		MetadataStores.add(spider.getId(), spider.getStores().getMetadataStore().build(context));
		BlobStores.add(spider.getId(), spider.getStores().getBlobStore().build(context));

		spider.getExtractors().getPages().forEach(ex -> {
			DocumentStores.add(spider.getId(), ex.getName(), ex.getDocumentStore().build(context));
		});

		// Init frontier
		Frontiers.add(spider.getId(), spider.getFrontier().build(context));
	}

	@Override
	public void pause() {

	}

	@Override
	public void kill() {

	}

	@Override
	public void register() {
		FrontierContainers.add(spider.getId(), this);
	}

	public void unregister() {
		FrontierContainers.remove(spider.getId());
	}
}
