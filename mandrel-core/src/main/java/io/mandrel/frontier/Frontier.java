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

import io.mandrel.common.net.Uri;
import io.mandrel.common.service.ObjectFactory;
import io.mandrel.common.service.TaskContext;
import io.mandrel.common.service.TaskContextAware;
import io.mandrel.frontier.revisit.RevisitStrategy;
import io.mandrel.frontier.revisit.RevisitStrategy.RevisitStrategyDefinition;
import io.mandrel.frontier.revisit.SimpleRevisitStrategy.SimpleRevisitStrategyDefinition;
import io.mandrel.frontier.store.FrontierStore;
import io.mandrel.frontier.store.FrontierStore.FrontierStoreDefinition;
import io.mandrel.frontier.store.impl.RedisFrontierStore.RedisFrontierStoreDefinition;
import io.mandrel.frontier.strategy.FrontierStrategy;
import io.mandrel.frontier.strategy.FrontierStrategy.FrontierStrategyDefinition;
import io.mandrel.frontier.strategy.SimpleFrontierStrategy.SimpleFrontierStrategyDefinition;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class Frontier extends TaskContextAware implements Checkable {

	public Frontier(TaskContext context) {
		super(context);
	}

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class FrontierDefinition implements ObjectFactory<Frontier>, Serializable {

		private static final long serialVersionUID = 7103095906121624004L;

		@JsonProperty("revisit")
		protected RevisitStrategyDefinition<? extends RevisitStrategy> revisit = new SimpleRevisitStrategyDefinition();

		@JsonProperty("store")
		protected FrontierStoreDefinition<? extends FrontierStore> store = new RedisFrontierStoreDefinition();

		@JsonProperty("strategy")
		private FrontierStrategyDefinition<? extends FrontierStrategy> strategy = new SimpleFrontierStrategyDefinition();

		public Frontier build(TaskContext context) {
			FrontierStrategy buildStrategy = strategy.build(context);
			FrontierStore buildStore = store.build(context);
			RevisitStrategy buildRevisit = revisit.build(context);
			buildStrategy.store(buildStore).revisit(buildRevisit);
			Frontier frontier = new Frontier(context).store(buildStore).revisit(buildRevisit).strategy(buildStrategy);
			return frontier;
		}
	}

	private RevisitStrategy revisit;
	private FrontierStore store;
	private FrontierStrategy strategy;

	public void init() {
		strategy.init();
	};

	public void destroy() {
		strategy.destroy();
	};

	public void pool(PoolCallback<Uri> poolCallback) {
		strategy.pool(poolCallback);
	};

	public void schedule(Uri uri) {
		strategy.schedule(uri);
	};

	public void schedule(Set<Uri> uris) {
		strategy.schedule(uris);
	};

	@Override
	public boolean check() {
		// TODO Auto-generated method stub
		return false;
	}
}
