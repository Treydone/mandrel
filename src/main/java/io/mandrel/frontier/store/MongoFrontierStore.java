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

import io.mandrel.common.service.TaskContext;

import java.net.URI;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

public class MongoFrontierStore extends FrontierStore {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class MongoFrontierStoreDefinition extends FrontierStoreDefinition<MongoFrontierStore> {
		private static final long serialVersionUID = -5715057009212205361L;

		@Override
		public String name() {
			return "mongo";
		}

		@Override
		public MongoFrontierStore build(TaskContext context) {
			return new MongoFrontierStore(context);
		}
	}

	public MongoFrontierStore(TaskContext context) {
		super(context);
	}

	@Override
	public Queue<URI> queue(String name) {
		return new MongoQueue<>(instance.getQueue(name));
	}

	@Override
	public void create(String defaultQueue) {

	}

	@Override
	public void finish(URI uri) {

	}

	@Override
	public void delete(URI uri) {

	}

	@Data
	public static class MongoQueue<T> implements Queue<T> {

		private final IQueue<T> raw;

		@Override
		public T pool() {
			return raw.poll();
		}

		@Override
		public void schedule(T t) {
			raw.add(t);
		}
	}

	@Override
	public boolean check() {
		return true;
	}
}
