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

import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.ObjectFactory;
import io.mandrel.common.service.TaskContext;
import io.mandrel.common.service.TaskContextAware;
import io.mandrel.frontier.PoolCallback;
import io.mandrel.frontier.revisit.RevisitStrategy;
import io.mandrel.frontier.store.FrontierStore;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public abstract class FrontierStrategy extends TaskContextAware implements Checkable {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static abstract class FrontierStrategyDefinition<FRONTIERSTRATEGY extends FrontierStrategy> implements NamedDefinition,
			ObjectFactory<FRONTIERSTRATEGY>, Serializable {
		private static final long serialVersionUID = -4024901085285125948L;
	}

	protected RevisitStrategy revisit;
	protected FrontierStore store;

	public FrontierStrategy(TaskContext context) {
		super(context);
	}

	public abstract void init();

	public abstract void destroy();

	public abstract void pool(PoolCallback<Uri> poolCallback);

	public abstract void schedule(Uri uri);

	public abstract void schedule(Set<Uri> uris);
}
