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
package io.mandrel.metrics;

import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.common.client.Clients;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
public class MetricsSyncer implements ApplicationListener<ContextStartedEvent> {

	@Autowired
	private MetricsService metricsService;
	@Autowired
	private Clients clients;
	@Autowired
	private DiscoveryClient discoveryClient;

	private final AtomicBoolean started = new AtomicBoolean();

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {
		started.compareAndSet(true, true);
	}

	@Scheduled(fixedRate = 5000)
	public void sync() {
		if (started.get()) {
			List<ServiceInstance> instances = discoveryClient.getInstances(ServiceIds.CONTROLLER);
			if (!CollectionUtils.isEmpty(instances)) {
				URI uri = instances.get(0).getUri();
				// TODO
				// clients.controllerClient().updateMetrics(metricsService.globalAccumulator(),
				// metricsService.spiderAccumulators(), uri);
			} else {
				log.warn("Can not find any controller");
			}
		}
	}
}
