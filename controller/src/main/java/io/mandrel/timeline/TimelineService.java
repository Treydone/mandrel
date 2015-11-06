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
package io.mandrel.timeline;

import io.mandrel.messaging.StompService;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TimelineService {

	private final TimelineRepository timelineRepository;
	private final StompService stompService;
	private final ScheduledExecutorService executor;

	public void add(Event event) {
		timelineRepository.add(event);
	}

	public List<Event> page(int from, int size) {
		return timelineRepository.page(from, size);
	}

	@PostConstruct
	public void init() {
		executor.submit(() -> pool());
	}

	public void pool() {
		timelineRepository.pool(event -> stompService.publish(event));
	}
}
