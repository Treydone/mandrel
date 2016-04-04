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

import io.mandrel.common.container.Container;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkerContainers {

	private final static Map<Long, WorkerContainer> workerContainers = new HashMap<>();

	public static Collection<WorkerContainer> list() {
		return workerContainers.values();
	}

	public static void add(long jobId, WorkerContainer workerContainer) {
		synchronized (workerContainers) {
			Container oldContainer = workerContainers.put(jobId, workerContainer);
			if (oldContainer != null) {
				try {
					oldContainer.kill();
				} catch (Exception e) {
					log.warn("Can not close", e);
				}
			}
		}
	}

	public static Optional<WorkerContainer> get(Long jobId) {
		return workerContainers.get(jobId) != null ? Optional.of(workerContainers.get(jobId)) : Optional.empty();
	}

	public static void remove(Long jobId) {
		synchronized (workerContainers) {
			Container oldContainer = workerContainers.remove(jobId);
			if (oldContainer != null) {
				try {
					oldContainer.kill();
				} catch (Exception e) {
					log.warn("Can not close", e);
				}
			}
		}
	}
}