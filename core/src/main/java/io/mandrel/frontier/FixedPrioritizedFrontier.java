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

import io.mandrel.common.service.TaskContext;
import io.mandrel.data.Link;
import io.mandrel.data.filters.link.BooleanLinkFilters;
import io.mandrel.data.filters.link.LinkFilter;
import io.mandrel.frontier.store.Queue;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class FixedPrioritizedFrontier extends Frontier {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class FixedPrioritizedFrontierDefinition extends FrontierDefinition<FixedPrioritizedFrontier> {
		private static final long serialVersionUID = -4024901085285125948L;

		@JsonProperty("priorities")
		private List<Priority> priorities = new ArrayList<Priority>();

		@Override
		public FixedPrioritizedFrontier build(TaskContext context) {
			return build(new FixedPrioritizedFrontier(context).priorities(priorities), context);
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
			priorities.get(idx).level(idx);

			// Create queue in store
				store().create("queue-" + idx);
			});
	}

	public FixedPrioritizedFrontier(TaskContext context) {
		super(context);
	}

	@Override
	public URI pool() {
		for (Priority p : priorities) {
			URI uri = create(p).pool();
			if (uri != null) {
				// duplicateUrlEliminator().markAsPending(uri);
				return uri;
			}
		}
		return null;
	}

	@Override
	public void schedule(URI uri) {
		priorities.stream().filter(p -> p.filter().isValid(new Link().uri(uri))).findFirst().ifPresent(p -> create(p));
	}

	@Override
	public void schedule(Set<URI> uris) {
		uris.forEach(uri -> schedule(uri));
	}

	private Queue<URI> create(Priority p) {
		return store().create("queue-" + p.level());
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

}
