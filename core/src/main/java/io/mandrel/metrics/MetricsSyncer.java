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

import io.mandrel.cluster.instance.StateService;
import io.mandrel.common.thrift.Clients;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Slf4j
@Component
public class MetricsSyncer {

	@Autowired
	private Accumulators accumulators;
	@Autowired
	private Clients clients;
	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private StateService stateService;

	@Scheduled(fixedRate = 5000)
	public void sync() {
		if (stateService.isStarted()) {

			Map<String, Long> total = Maps.newHashMap();
			total.putAll(accumulators.globalAccumulator().tick());
			total.putAll(accumulators.nodeAccumulator().tick());
			accumulators.spiderAccumulators().forEach((spiderId, acc) -> total.putAll(acc.tick()));

			try {
				clients.onRandomController().with(controller -> controller.updateMetrics(total));
			} catch (Exception e) {
				log.debug("Can not update metrics due to", e);
			}
		}
	}
}
