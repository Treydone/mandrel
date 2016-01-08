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
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.ObjectFactory;
import io.mandrel.common.service.TaskContext;
import io.mandrel.frontier.store.FetchRequest;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class SimpleFrontier extends Frontier {

	private static final String DEFAULT_QUEUE = "default";

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class SimpleFrontierDefinition extends FrontierDefinition<SimpleFrontier> implements NamedDefinition, ObjectFactory<SimpleFrontier>,
			Serializable {
		private static final long serialVersionUID = -4024901085285125948L;

		@Override
		public SimpleFrontier build(TaskContext context) {
			return build(new SimpleFrontier(context), context);
		}

		@Override
		public String name() {
			return "simple";
		}
	}

	public SimpleFrontier(TaskContext context) {
		super(context);
	}

	public void init() {
		store().create(DEFAULT_QUEUE);
	}

	public void destroy() {
		try {
			store().destroy(DEFAULT_QUEUE);
		} catch (Exception e) {
			log.warn("Unable to destory queue", e);
		}
	}

	@Override
	public void pool(PoolCallback<Uri> poolCallback) {
		store.pool(FetchRequest.of(DEFAULT_QUEUE, poolCallback));
	}

	@Override
	public void schedule(Uri uri) {
		store.schedule(DEFAULT_QUEUE, uri);
	}

	@Override
	public void schedule(Set<Uri> uris) {
		store.schedule(DEFAULT_QUEUE, uris);
	}

	@Override
	public boolean check() {
		return true;
	}
}
