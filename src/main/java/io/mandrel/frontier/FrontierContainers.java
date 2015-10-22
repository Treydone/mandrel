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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FrontierContainers {

	private final static ConcurrentHashMap<Long, FrontierContainer> frontierContainers = new ConcurrentHashMap<>();

	public static Iterable<FrontierContainer> list() {
		return frontierContainers.values();
	}

	public static void add(long spiderId, FrontierContainer FrontierContainer) {
		frontierContainers.put(spiderId, FrontierContainer);
	}

	public static Optional<FrontierContainer> get(Long spiderId) {
		return frontierContainers.get(spiderId) != null ? Optional.of(frontierContainers.get(spiderId)) : Optional.empty();
	}

	public static void remove(Long spiderId) {
		frontierContainers.remove(spiderId);
	}
}