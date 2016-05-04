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
package io.mandrel.frontier.store.impl;

import io.mandrel.common.data.Job;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.frontier.store.FetchRequest;
import io.mandrel.frontier.store.impl.RedisFrontierStore.RedisFrontierStoreDefinition;

import org.junit.Test;

public class RedisFrontierStoreTest {

	@Test
	public void test() throws InterruptedException {

		Job job = new Job().setId(31);
		TaskContext taskContext = new TaskContext();
		taskContext.setDefinition(job);

		RedisFrontierStoreDefinition definition = new RedisFrontierStoreDefinition();
		RedisFrontierStore store = definition.build(taskContext);

		store.create("default");
		store.schedule("default", Uri.create("test://test1"));
		store.schedule("default", Uri.create("test://test2"));
		store.schedule("default", Uri.create("test://test3"));
		store.schedule("default", Uri.create("test://test4"));
		Thread.sleep(500);

		store.pool(FetchRequest.of("default", (uri, name) -> {
			System.err.println("1:" + uri);
		}));
		store.pool(FetchRequest.of("default", (uri, name) -> {
			System.err.println("2:" + uri);
		}));
		store.pool(FetchRequest.of("default", (uri, name) -> {
			System.err.println("3:" + uri);
		}));
		store.pool(FetchRequest.of("default", (uri, name) -> {
			System.err.println("4:" + uri);
		}));
		store.pool(FetchRequest.of("default", (uri, name) -> {
			System.err.println("5:" + uri);
		}));
		store.pool(FetchRequest.of("default", (uri, name) -> {
			System.err.println("6:" + uri);
		}));
		Thread.sleep(4000);
	}
}
