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
package io.mandrel.endpoints.rest;

import io.mandrel.common.data.Spider;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.endpoints.contracts.WorkerContract;
import io.mandrel.frontier.FrontierClient;
import io.mandrel.metrics.MetricsService;
import io.mandrel.worker.WorkerContainer;
import io.mandrel.worker.WorkerContainers;

import java.net.URI;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WorkerResource implements WorkerContract {

	private final ExtractorService extractorService;
	private final MetricsService metricsService;
	private final FrontierClient frontierClient;
	private final DiscoveryClient discoveryClient;
	private final HazelcastInstance instance;

	@Override
	public void create(Spider spider, URI target) {
		WorkerContainer container = new WorkerContainer(extractorService, metricsService, spider, frontierClient, discoveryClient, instance);
		container.register();
	}

	@Override
	public void start(Long id, URI target) {
		WorkerContainers.get(id).ifPresent(c -> c.start());
	}

	@Override
	public void pause(Long id, URI target) {
		WorkerContainers.get(id).ifPresent(c -> c.pause());
	}

	@Override
	public void kill(Long id, URI target) {
		WorkerContainers.get(id).ifPresent(c -> c.kill());
	}
}
