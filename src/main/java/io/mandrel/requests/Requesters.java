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
package io.mandrel.requests;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

public class Requesters {

	private final static Map<Long, Map<String, Requester>> requesters = new HashMap<>();

	public static Iterable<Map<String, Requester>> list() {
		return ImmutableList.copyOf(requesters.values());
	}

	public static void add(long spiderId, Requester requester) {
		synchronized (requesters) {
			Map<String, Requester> map = requesters.get(spiderId);
			if (map == null) {
				map = new HashMap<>();
				requesters.put(spiderId, map);
			}
			map.putAll(requester.getProtocols().stream().collect(Collectors.toMap(p -> p, p -> requester)));
			requesters.put(spiderId, map);
		}
	}

	public static Optional<Map<String, Requester>> of(Long spiderId) {
		return requesters.get(spiderId) != null ? Optional.of(requesters.get(spiderId)) : Optional.empty();
	}

	public static void remove(Long spiderId) {
		synchronized (requesters) {
			requesters.remove(spiderId);
		}
	}

	public static Optional<Requester> of(Long spiderId, String protocol) {
		return requesters.get(spiderId) != null ? (requesters.get(spiderId).get(protocol) != null ? Optional.of(requesters.get(spiderId).get(protocol))
				: Optional.empty()) : Optional.empty();
	}
}
