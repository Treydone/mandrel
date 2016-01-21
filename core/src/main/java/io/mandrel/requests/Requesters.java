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

import io.mandrel.common.data.Strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Requesters {

	private final static Map<Long, Map<String, Requester<? extends Strategy>>> requesters = new HashMap<>();
	private final static Map<String, Requester<? extends Strategy>> globalRequesters = new HashMap<>();

	public static Iterable<Map<String, Requester<? extends Strategy>>> list() {
		return requesters.values();
	}

	public static void add(Requester<? extends Strategy> requester) {
		synchronized (globalRequesters) {
			globalRequesters.putAll(requester.getProtocols().stream().collect(Collectors.toMap(p -> p, p -> requester)));
		}
	}

	public static Optional<Requester<? extends Strategy>> of(String protocol) {
		return globalRequesters.get(protocol) != null ? Optional.of(globalRequesters.get(protocol)) : Optional.empty();
	}

	public static void add(long spiderId, Requester<? extends Strategy> requester) {
		synchronized (requesters) {
			requesters.putIfAbsent(spiderId, new HashMap<>());
			Map<String, Requester<? extends Strategy>> map = requesters.get(spiderId);
			requester.getProtocols().forEach(protocol -> {

				Requester<? extends Strategy> oldRequester = map.put(protocol, requester);
				if (oldRequester != null) {
					try {
						oldRequester.close();
					} catch (Exception e) {
						log.warn("Can not close", e);
					}
				}
			});
			map.putAll(requester.getProtocols().stream().collect(Collectors.toMap(p -> p, p -> requester)));
			requesters.put(spiderId, map);
		}
	}

	public static Optional<Map<String, Requester<? extends Strategy>>> of(Long spiderId) {
		return requesters.get(spiderId) != null ? Optional.of(requesters.get(spiderId)) : Optional.empty();
	}

	public static void remove(Long spiderId) {
		synchronized (requesters) {
			Map<String, Requester<? extends Strategy>> oldRequesters = requesters.remove(spiderId);
			oldRequesters.forEach((protocol, req) -> {
				try {
					req.close();
				} catch (Exception e) {
					log.warn("Can not close", e);
				}
			});
		}
	}

	public static Optional<Requester<? extends Strategy>> of(Long spiderId, String protocol) {
		return requesters.get(spiderId) != null ? (requesters.get(spiderId).get(protocol) != null ? Optional.of(requesters.get(spiderId).get(protocol))
				: Optional.empty()) : Optional.empty();
	}
}
