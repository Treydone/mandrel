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
package io.mandrel.controller;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerContainers {

	private final static ConcurrentHashMap<Long, ControllerContainer> controllerContainers = new ConcurrentHashMap<>();

	public static Iterable<ControllerContainer> list() {
		return controllerContainers.values();
	}

	public static void add(long spiderId, ControllerContainer ControllerContainer) {
		controllerContainers.put(spiderId, ControllerContainer);
	}

	public static Optional<ControllerContainer> get(Long spiderId) {
		return controllerContainers.get(spiderId) != null ? Optional.of(controllerContainers.get(spiderId)) : Optional.empty();
	}

	public static void remove(Long spiderId) {
		controllerContainers.remove(spiderId);
	}
}