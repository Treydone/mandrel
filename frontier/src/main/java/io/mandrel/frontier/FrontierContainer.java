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

import io.mandrel.common.client.Clients;
import io.mandrel.common.container.Container;
import io.mandrel.common.data.Spider;
import io.mandrel.common.service.TaskContext;
import io.mandrel.data.source.Source;
import io.mandrel.metadata.MetadataStores;
import io.mandrel.metrics.Accumulators;
import lombok.Data;
import lombok.experimental.Accessors;

import org.springframework.cloud.client.discovery.DiscoveryClient;

@Data
@Accessors(chain = true, fluent = true)
public class FrontierContainer implements Container {

	private final Spider spider;
	private final Accumulators accumulators;
	private final Clients clients;
	private final DiscoveryClient discoveryClient;

	private TaskContext context = new TaskContext();
	private Frontier frontier;

	public FrontierContainer(Spider spider, Accumulators accumulators, Clients clients, DiscoveryClient discoveryClient) {
		super();
		this.spider = spider;
		this.accumulators = accumulators;
		this.clients = clients;
		this.discoveryClient = discoveryClient;
		init();
	}

	@Override
	public String type() {
		return "frontier";
	}

	public void init() {

		// Create context
		context.setDefinition(spider);

		// Init stores
		MetadataStores.add(spider.getId(), spider.getStores().getMetadataStore().build(context));

		// Init frontier
		frontier = spider.getFrontier().build(context);

	}

	@Override
	public void start() {

		// Init sources
		spider.getSources().forEach(s -> {
			Source source = s.build(context);
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
		accumulators.destroy(spider.getId());
	}

	@Override
	public void register() {
		FrontierContainers.add(spider.getId(), this);
	}

	public void unregister() {
		FrontierContainers.remove(spider.getId());
	}

}
