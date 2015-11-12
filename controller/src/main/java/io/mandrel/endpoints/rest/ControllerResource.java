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

import io.mandrel.endpoints.contracts.AdminContract;
import io.mandrel.metrics.MetricsRepository;
import io.mandrel.timeline.Event;
import io.mandrel.timeline.TimelineService;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ControllerResource implements AdminContract {

	private final TimelineService timelineService;

	private final MetricsRepository metricsRepository;

	public void addEvent(@RequestBody Event event, URI target) {
		timelineService.add(event);
	}

	@Override
	public void updateMetrics(Map<String, Long> accumulators, URI uri) {
		metricsRepository.sync(accumulators);
	}
}
