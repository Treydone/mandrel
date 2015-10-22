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
package io.mandrel.worker;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

public class WorkerContainers {

	private final static Map<Long, WorkerContainer> workerContainers = new HashMap<>();

	public static Iterable<WorkerContainer> list() {
		return ImmutableList.copyOf(workerContainers.values());
	}

	public static void add(long spiderId, WorkerContainer WorkerContainer) {
		synchronized (workerContainers) {
			workerContainers.put(spiderId, WorkerContainer);
		}
	}

	public static Optional<WorkerContainer> get(Long spiderId) {
		return workerContainers.get(spiderId) != null ? Optional.of(workerContainers.get(spiderId)) : Optional.empty();
	}

	public static void remove(Long spiderId) {
		synchronized (workerContainers) {
			workerContainers.remove(spiderId);
		}
	}
}