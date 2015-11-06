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

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MetricsSyncer {

	private final MetricsService metricsService;
	private final Clients clients;
	private final DiscoveryClient discoveryClient;

	@Scheduled(fixedRate = 5000)
	public void sync() {

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
