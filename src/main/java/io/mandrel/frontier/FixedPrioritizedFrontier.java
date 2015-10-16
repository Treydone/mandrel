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

import io.mandrel.data.filters.link.BooleanLinkFilters;
import io.mandrel.data.filters.link.LinkFilter;
import io.mandrel.data.spider.Link;
import io.mandrel.frontier.store.Queue;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = false)
public class FixedPrioritizedFrontier extends Frontier {

	private static final long serialVersionUID = -4055424223863734294L;

	@JsonProperty("priorities")
	private List<Priority> priorities = new ArrayList<Priority>();

	@Override
	public void create() {

		// Add default if not present
		if (priorities.stream().noneMatch(p -> p.defaultPriority())) {
			priorities.add(Priority.of(new BooleanLinkFilters.TrueFilter(), true));
		}

		IntStream.range(0, priorities.size()).forEach(idx -> {
			priorities.get(idx).level(idx);
		});
	}

	@Override
	public URI pool() {
		for (Priority p : priorities) {
			URI uri = queue(p).pool();
			if (uri != null) {
				getDuplicateUrlEliminator().markAsPending("pendings", uri);
				return uri;
			}
		}
		return null;
	}

	@Override
	public void schedule(URI uri) {
		priorities.stream().filter(p -> p.filter().isValid(new Link().uri(uri))).findFirst().ifPresent(p -> queue(p));
	}

	private Queue<URI> queue(Priority p) {
		return getStore().queue("queue-" + p.level());
	}

	@Override
	public void finished(URI uri) {
		getDuplicateUrlEliminator().removePending("pendings", uri);
		getStore().finish(uri);
	}

	@Override
	public void delete(URI uri) {
		getDuplicateUrlEliminator().removePending("pendings", uri);
		getStore().delete(uri);
	}

	@Override
	public String name() {
		return "priority";
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
}
