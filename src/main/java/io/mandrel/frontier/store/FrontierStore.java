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
package io.mandrel.frontier.store;

import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.common.service.ObjectFactory;
import io.mandrel.common.service.TaskContext;
import io.mandrel.common.service.TaskContextAware;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.net.URI;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public abstract class FrontierStore extends TaskContextAware implements Checkable {

	public FrontierStore(TaskContext context) {
		super(context);
	}

	@Data
	public static abstract class FrontierStoreDefinition<FRONTIERSTORE extends FrontierStore> implements NamedDefinition, ObjectFactory<FRONTIERSTORE>,
			Serializable {
		private static final long serialVersionUID = -8064877296016844646L;
	}

	public abstract Queue<URI> queue(String name);

	public abstract void create(String defaultQueue);

	public abstract void finish(URI uri);

	public abstract void delete(URI uri);
}
