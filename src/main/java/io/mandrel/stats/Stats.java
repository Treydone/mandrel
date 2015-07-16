package io.mandrel.stats;

import java.util.HashMap;
import java.util.Map;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IMap;

public class Stats {

	private final IAtomicLong nbPages;
	private final IMap<String, Boolean> pendings;
	private final IAtomicLong totalSize;
	private final IAtomicLong totalTimeToFetch;

	private final IAtomicLong readTimeout;
	private final IAtomicLong connectTimeout;
	private final IAtomicLong connectException;

	private final long spiderId;

	private final transient HazelcastInstance instance;

	public Stats(HazelcastInstance instance, long spiderId) {
		this.instance = instance;
		this.spiderId = spiderId;

		nbPages = instance.getAtomicLong(getKey(spiderId) + "-nbPages");

		if (instance.getConfig().getQueueConfigs().containsKey("pendings-" + spiderId)) {
			// Create map of pendings with TTL of 10 secs
			MapConfig mapConfig = new MapConfig();
			mapConfig.setName("pendings-" + spiderId).setBackupCount(10).setTimeToLiveSeconds(1).setStatisticsEnabled(true);
			instance.getConfig().addMapConfig(mapConfig);
		}
		pendings = instance.getMap("pendings-" + spiderId);

		totalSize = instance.getAtomicLong(getKey(spiderId) + "-totalSize");
		totalTimeToFetch = instance.getAtomicLong(getKey(spiderId) + "-totalTimeToFetch");

		readTimeout = instance.getAtomicLong(getKey(spiderId) + "-readTimeout");
		connectTimeout = instance.getAtomicLong(getKey(spiderId) + "-connectTimeout");
		connectException = instance.getAtomicLong(getKey(spiderId) + "-connectException");
	}

	public void delete() {
		// TODO
		totalSize.destroy();
		nbPages.destroy();
		totalTimeToFetch.destroy();
		pendings.destroy();

		readTimeout.destroy();
		connectTimeout.destroy();
		connectException.destroy();

		instance.<Integer> getSet(getKey(spiderId) + "-nbPagesByStatus").forEach(
				httpStatus -> instance.getAtomicLong(getKey(spiderId) + "-status-" + httpStatus).destroy());
		instance.<String> getSet(getKey(spiderId) + "-documentsByExtractor").forEach(
				extractor -> instance.getAtomicLong(getKey(spiderId) + "-extractor-" + extractor).destroy());
	}

	protected String getKey(long spiderId) {
		String key = "spider-" + spiderId;
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

	public long incPageForStatus(int httpStatus) {
		return instance.getAtomicLong(getKey(spiderId) + "-status-" + httpStatus).addAndGet(1);
	}

	public long incDocumentForExtractor(String extractor, int inc) {
		return instance.getAtomicLong(getKey(spiderId) + "-extractor-" + extractor).addAndGet(inc);
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

	public long getAverageBandwidth() {
		return getTotalSize() / getTotalTimeToFetch();
	}

	public Map<Integer, Long> getPagesByStatus() {
		Map<Integer, Long> result = new HashMap<>();
		instance.<Integer> getSet(getKey(spiderId) + "-nbPagesByStatus").forEach(
				httpStatus -> result.put(httpStatus, instance.getAtomicLong(getKey(spiderId) + "-status-" + httpStatus).get()));
		return result;
	}

	public Map<String, Long> getDocumentsByExtractor() {
		Map<String, Long> result = new HashMap<>();
		instance.<String> getSet(getKey(spiderId) + "-documentsByExtractor").forEach(
				extractor -> result.put(extractor, instance.getAtomicLong(getKey(spiderId) + "-extractor-" + extractor).get()));
		return result;
	}
}
