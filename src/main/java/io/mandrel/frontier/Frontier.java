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

import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.common.service.ObjectFactory;
import io.mandrel.common.service.TaskContext;
import io.mandrel.common.service.TaskContextAware;
import io.mandrel.due.DuplicateUrlEliminator;
import io.mandrel.due.DuplicateUrlEliminator.DuplicateUrlEliminatorDefinition;
import io.mandrel.frontier.revisit.RevisitStrategy;
import io.mandrel.frontier.revisit.SimpleRevisitStrategy;
import io.mandrel.frontier.store.FrontierStore;
import io.mandrel.frontier.store.FrontierStore.FrontierStoreDefinition;
import io.mandrel.frontier.store.InternalFrontierStore.InternalFrontierStoreDefinition;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.net.URI;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public abstract class Frontier extends TaskContextAware implements Checkable {

	public Frontier(TaskContext context) {
		super(context);
	}

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static abstract class FrontierDefinition<FRONTIER extends Frontier> implements NamedDefinition, ObjectFactory<FRONTIER>, Serializable {

		private static final long serialVersionUID = 7103095906121624004L;

		@JsonProperty("politeness")
		protected Politeness politeness = new Politeness();

		@JsonProperty("revist")
		protected RevisitStrategy revisit = new SimpleRevisitStrategy();

		@JsonProperty("store")
		protected FrontierStoreDefinition<? extends FrontierStore> store = new InternalFrontierStoreDefinition();

		@JsonProperty("due")
		protected DuplicateUrlEliminatorDefinition duplicateUrlEliminator;

		public FRONTIER build(FRONTIER frontier, TaskContext context) {
			frontier.duplicateUrlEliminator(duplicateUrlEliminator.build(context)).store(store.build(context)).politeness(politeness).revisit(revisit);
			return frontier;
		}
	}

	protected Politeness politeness;
	protected RevisitStrategy revisit;
	protected FrontierStore store;
	protected DuplicateUrlEliminator duplicateUrlEliminator;

	public abstract void create();

	public abstract URI pool();

	public abstract void schedule(URI uri);

	public abstract void finished(URI uri);

	public abstract void delete(URI uri);

}
