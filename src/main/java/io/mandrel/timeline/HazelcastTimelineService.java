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

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HazelcastTimelineService implements TimelineService {

	private final HazelcastInstance instance;

	private final StompService stompService;

	@Override
	public void add(Event event) {
		instance.getList("timeline").add(event);
		stompService.publish(event);
	}

	@Override
	public List<Event> page(int from, int size) {
		IList<Event> timeline = instance.getList("timeline");
		int total = timeline.size();

		if (from > total) {
			return null;
		}

		List<Event> subList = timeline.subList(Math.max(0, total - from - size), total - from);
		return Lists.reverse(subList);
	}
}
