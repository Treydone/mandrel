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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

public class Frontiers {

	private final static Map<Long, Frontier> frontiers = new HashMap<>();

	public static Iterable<Frontier> list() {
		return ImmutableList.copyOf(frontiers.values());
	}

	public static void add(long spiderId, Frontier frontier) {
		synchronized (frontiers) {
			frontiers.put(spiderId, frontier);
		}
	}

	public static Optional<Frontier> get(Long spiderId) {
		return frontiers.get(spiderId) != null ? Optional.of(frontiers.get(spiderId)) : Optional.empty();
	}

	public static void remove(Long spiderId) {
		synchronized (frontiers) {
			frontiers.remove(spiderId);
		}
	}
}
