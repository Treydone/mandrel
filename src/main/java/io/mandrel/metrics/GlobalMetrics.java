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

import java.util.HashMap;
import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

public class GlobalMetrics {

	private final IAtomicLong nbPagesTotal;
	private final IAtomicLong totalSizeTotal;
	private final HazelcastInstance instance;

	public GlobalMetrics(HazelcastInstance instance) {
		this.instance = instance;
		nbPagesTotal = instance.getAtomicLong("nbPages");
		totalSizeTotal = instance.getAtomicLong("totalSize");
	}

	public long incNbPages() {
		return nbPagesTotal.incrementAndGet();
	}

	public long incTotalSize(long size) {
		return totalSizeTotal.addAndGet(size);
	}

	public long getNbPagesTotal() {
		return nbPagesTotal.get();
	}

	public long getTotalSizeTotal() {
		return totalSizeTotal.get();
	}

	public Map<Integer, Long> getPagesByStatus() {
		Map<Integer, Long> result = new HashMap<>();
		instance.<Integer> getSet("nbPagesByStatus").forEach(httpStatus -> result.put(httpStatus, instance.getAtomicLong("status-" + httpStatus).get()));
		return result;
	}

	public long incPageForStatus(int httpStatus) {
		return instance.getAtomicLong("status-" + httpStatus).addAndGet(1);
	}

	public Map<Integer, Long> getPagesByHost() {
		Map<Integer, Long> result = new HashMap<>();
		instance.<Integer> getSet("nbPagesByHost").forEach(host -> result.put(host, instance.getAtomicLong("hosts-" + host).get()));
		return result;
	}

	public long incPageForHost(String host) {
		return instance.getAtomicLong("hosts-" + host).addAndGet(1);
	}

	public Map<Integer, Long> getPagesByContentType() {
		Map<Integer, Long> result = new HashMap<>();
		instance.<Integer> getSet("nbPagesByContentType").forEach(
				contentType -> result.put(contentType, instance.getAtomicLong("contentType-" + contentType).get()));
		return result;
	}

	public long incPageForContentType(String contentType) {
		return instance.getAtomicLong("contentType-" + contentType).addAndGet(1);
	}

}
