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
import java.util.Set;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IMap;

public class SpiderMetrics {

	private final IAtomicLong nbPages;
	private final IMap<String, Boolean> pendings;
	private final IAtomicLong totalSize;
	private final IAtomicLong totalTimeToFetch;

	private final IAtomicLong readTimeout;
	private final IAtomicLong connectTimeout;
	private final IAtomicLong connectException;

	private final long spiderId;

	private final transient HazelcastInstance instance;

	public SpiderMetrics(HazelcastInstance instance, long spiderId) {
		this.instance = instance;
		this.spiderId = spiderId;

		Set<String> keys = instance.getSet(getKey(spiderId, "-keys"));

		if (instance.getConfig().getQueueConfigs().containsKey("pendings-" + spiderId)) {
			// Create map of pendings with TTL of 10 secs
			MapConfig mapConfig = new MapConfig();
			mapConfig.setName("pendings-" + spiderId).setBackupCount(10).setTimeToLiveSeconds(1).setStatisticsEnabled(true);
			instance.getConfig().addMapConfig(mapConfig);
		}
		pendings = instance.getMap("pendings-" + spiderId);

		nbPages = instance.getAtomicLong(getKey(spiderId, "-nbPages"));
		keys.add(getKey(spiderId, "-nbPages"));
		totalSize = instance.getAtomicLong(getKey(spiderId, "-totalSize"));
		keys.add(getKey(spiderId, "-totalSize"));
		totalTimeToFetch = instance.getAtomicLong(getKey(spiderId, "-totalTimeToFetch"));
		keys.add(getKey(spiderId, "-totalTimeToFetch"));

		readTimeout = instance.getAtomicLong(getKey(spiderId, "-readTimeout"));
		keys.add(getKey(spiderId, "-readTimeout"));
		connectTimeout = instance.getAtomicLong(getKey(spiderId, "-connectTimeout"));
		keys.add(getKey(spiderId, "-connectTimeout"));
		connectException = instance.getAtomicLong(getKey(spiderId, "-connectException"));
		keys.add(getKey(spiderId, "-connectException"));
	}

	public void delete() {
		instance.<String> getSet(getKey(spiderId, "-keys")).forEach(key -> instance.getAtomicLong(key).destroy());

		instance.<Integer> getSet(getKey(spiderId, "-nbPagesByStatus")).forEach(
				httpStatus -> instance.getAtomicLong(getKey(spiderId, "-status-" + httpStatus)).destroy());
		instance.<String> getSet(getKey(spiderId, "-documentsByExtractor")).forEach(
				extractor -> instance.getAtomicLong(getKey(spiderId, "-extractor-" + extractor)).destroy());
	}

	protected String getKey(long spiderId, String prefix) {
		String key = "metrics-spider-" + spiderId + prefix;
		return key;
	}

	public long incConnectException() {
		return connectException.incrementAndGet();
	}

	public long incReadTimeout() {
		return readTimeout.incrementAndGet();
	}

	public long incConnectTimeout() {
		return connectTimeout.incrementAndGet();
	}

	public long incNbPages() {
		return nbPages.incrementAndGet();
	}

	public long incTotalSize(long size) {
		return totalSize.addAndGet(size);
	}

	public long incTotalTimeToFetch(long time) {
		return totalTimeToFetch.addAndGet(time);
	}

	public long getNbPendingPages() {
		return pendings.size();
	}

	public long getNbPages() {
		return nbPages.get();
	}

	public long getReadTimeout() {
		return readTimeout.get();
	}

	public long getConnectTimeout() {
		return connectTimeout.get();
	}

	public long getConnectException() {
		return connectException.get();
	}

	public long getTotalSize() {
		return totalSize.get();
	}

	public long getTotalTimeToFetch() {
		return totalTimeToFetch.get();
	}

	public long getAveragePageSize() {
		return getTotalSize() / getNbPages();
	}

	public long getAverageTimeToFetch() {
		return getTotalTimeToFetch() / getNbPages();
	}

	public long incPageForStatus(int httpStatus) {
		return instance.getAtomicLong(getKey(spiderId, "-status-" + httpStatus)).addAndGet(1);
	}

	public Map<Integer, Long> getPagesByStatus() {
		Map<Integer, Long> result = new HashMap<>();
		instance.<Integer> getSet(getKey(spiderId, "-nbPagesByStatus")).forEach(
				httpStatus -> result.put(httpStatus, instance.getAtomicLong(getKey(spiderId, "-status-" + httpStatus)).get()));
		return result;
	}

	public long incDocumentForExtractor(String extractor, int inc) {
		return instance.getAtomicLong(getKey(spiderId, "-extractor-" + extractor)).addAndGet(inc);
	}

	public Map<String, Long> getDocumentsByExtractor() {
		Map<String, Long> result = new HashMap<>();
		instance.<String> getSet(getKey(spiderId, "-documentsByExtractor")).forEach(
				extractor -> result.put(extractor, instance.getAtomicLong(getKey(spiderId, "-extractor-" + extractor)).get()));
		return result;
	}

	public Map<Integer, Long> getPagesByHost() {
		Map<Integer, Long> result = new HashMap<>();
		instance.<Integer> getSet(getKey(spiderId, "-nbPagesByHost")).forEach(
				host -> result.put(host, instance.getAtomicLong(getKey(spiderId, "-hosts-" + host)).get()));
		return result;
	}

	public long incPageForHost(String host) {
		return instance.getAtomicLong(getKey(spiderId, "-hosts-" + host)).addAndGet(1);
	}

	public Map<Integer, Long> getPagesByContentType() {
		Map<Integer, Long> result = new HashMap<>();
		instance.<Integer> getSet(getKey(spiderId, "-nbPagesByContentType")).forEach(
				contentType -> result.put(contentType, instance.getAtomicLong(getKey(spiderId, "-contentType-" + contentType)).get()));
		return result;
	}

	public long incPageForContentType(String contentType) {
		return instance.getAtomicLong(getKey(spiderId, "-contentType-" + contentType)).addAndGet(1);
	}
}
