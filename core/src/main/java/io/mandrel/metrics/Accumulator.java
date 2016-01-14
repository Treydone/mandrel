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
package io.mandrel.metrics;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Maps;

public class Accumulator {

	private ConcurrentHashMap<String, AtomicLong> countMap = new ConcurrentHashMap<>();

	public void add(String key, long n) {
		AtomicLong value = countMap.get(key);
		if (value == null) {
			value = countMap.putIfAbsent(key, new AtomicLong(n));
		}
		if (value != null) {
			value.addAndGet(n);
		}
	}

	public Map<String, Long> tick() {
		Map<String, Long> results = Collections.unmodifiableMap(Maps.transformValues(countMap, v -> v.get()));
		synchronized (countMap) {
			countMap.forEach((k, v) -> v.set(0));
		}
		return results;
	}
}
