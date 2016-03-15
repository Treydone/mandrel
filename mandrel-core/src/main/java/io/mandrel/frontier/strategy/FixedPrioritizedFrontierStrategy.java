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
package io.mandrel.frontier.strategy;

import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.data.Link;
import io.mandrel.data.filters.link.BooleanLinkFilters;
import io.mandrel.data.filters.link.LinkFilter;
import io.mandrel.frontier.PoolCallback;
import io.mandrel.frontier.store.FetchRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Slf4j
@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class FixedPrioritizedFrontierStrategy extends FrontierStrategy {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class FixedPrioritizedFrontierStrategyDefinition extends FrontierStrategyDefinition<FixedPrioritizedFrontierStrategy> {
		private static final long serialVersionUID = -4024901085285125948L;

		@JsonProperty("priorities")
		private List<Priority> priorities = new ArrayList<Priority>();

		@Override
		public FixedPrioritizedFrontierStrategy build(TaskContext context) {
			return new FixedPrioritizedFrontierStrategy(context).priorities(priorities);
		}

		@Override
		public String name() {
			return "priority";
		}
	}

	private List<Priority> priorities = new ArrayList<Priority>();

	public void init() {
		// Add default if not present
		if (priorities.stream().noneMatch(p -> p.defaultPriority())) {
			priorities.add(Priority.of(new BooleanLinkFilters.TrueFilter(), true));
		}

		IntStream.range(0, priorities.size()).forEach(idx -> {
			Priority priority = priorities.get(idx);
			priority.level(idx);

			// Create queue in store
				store().create(getQueue(priority));
			});
	}

	public void destroy() {
		priorities.stream().forEach(priority -> {
			try {
				store().destroy(getQueue(priority));
			} catch (Exception e) {
				log.warn("Unable to destory queue", e);
			}
		});
	}

	public FixedPrioritizedFrontierStrategy(TaskContext context) {
		super(context);
	}

	@Override
	public void pool(PoolCallback<Uri> poolCallback) {
		pool(0, poolCallback);
	}

	public void pool(int i, PoolCallback<Uri> poolCallback) {
		Priority priority = priorities.get(i);

		PoolCallback<Uri> chidlPoolCallback = (uri, name) -> {
			if (uri != null) {
				poolCallback.on(uri, name);
			} else {
				int index = i;
				index++;

				if (index < priorities.size() - 1) {
					pool(index, poolCallback);
				} else {
					poolCallback.on(null, name);
				}
			}
		};
		store.pool(FetchRequest.of(getQueue(priority), chidlPoolCallback));
	}

	@Override
	public void schedule(Uri uri) {
		priorities.stream().filter(p -> p.filter().isValid(new Link().setUri(uri))).findFirst().ifPresent(p -> store.schedule(getQueue(p), uri));
	}

	@Override
	public void schedule(Set<Uri> uris) {
		uris.forEach(uri -> schedule(uri));
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class Priority implements Serializable {

		private static final long serialVersionUID = 3354016226554882889L;

		@Getter(onMethod = @__(@JsonProperty("level")))
		@Setter
		private int level;

		@JsonProperty("filter")
		private LinkFilter filter;

		@JsonProperty("default")
		private boolean defaultPriority = false;

		@JsonCreator
		public static Priority of(@JsonProperty("filter") LinkFilter filter, @JsonProperty("default") boolean defaultPriority) {
			return new Priority().filter(!defaultPriority ? filter : new BooleanLinkFilters.TrueFilter()).defaultPriority(defaultPriority);
		}
	}

	@Override
	public boolean check() {
		return true;
	}

	private String getQueue(Priority priority) {
		return "queue-" + priority.level();
	}
}
