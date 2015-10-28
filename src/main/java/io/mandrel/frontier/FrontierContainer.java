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

import io.mandrel.common.container.Container;
import io.mandrel.common.data.Spider;
import io.mandrel.common.service.TaskContext;
import io.mandrel.data.source.Source;
import io.mandrel.metadata.MetadataStores;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import com.hazelcast.core.HazelcastInstance;

@Data
@Accessors(chain = true, fluent = true)
@RequiredArgsConstructor
public class FrontierContainer implements Container {

	private final Spider spider;
	private final HazelcastInstance instance;

	private Frontier frontier;

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

		// Init frontier
		frontier = spider.getFrontier().build(context);

		// Init sources
		spider.getSources().forEach(s -> {
			Source source = s.build(context);
			// TODO
				if (!source.singleton() && source.check()) {
					source.register(uri -> {
						frontier.schedule(uri);
					});
				}
			});

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
