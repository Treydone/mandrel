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
	private final Map<Integer, IAtomicLong> nbPagesByStatus = new HashMap<>();
	private final Map<String, IAtomicLong> documentsByExtractor = new HashMap<>();
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
	}

	protected String getKey(long spiderId) {
		String key = "spider-" + spiderId;
		return key;
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
		IAtomicLong iAtomicLong = nbPagesByStatus.get(Integer.valueOf(httpStatus));
		if (iAtomicLong == null) {
			iAtomicLong = instance.getAtomicLong(getKey(spiderId) + "-status-" + httpStatus);
			nbPagesByStatus.put(httpStatus, iAtomicLong);
		}
		return iAtomicLong.addAndGet(1);
	}

	public long incDocumentForExtractor(String extractor, int inc) {
		IAtomicLong iAtomicLong = documentsByExtractor.get(extractor);
		if (iAtomicLong == null) {
			iAtomicLong = instance.getAtomicLong(getKey(spiderId) + "-extractor-" + extractor);
			documentsByExtractor.put(extractor, iAtomicLong);
		}
		return iAtomicLong.addAndGet(inc);
	}

	public long getNbPendingPages() {
		return pendings.size();
	}

	public long getNbPages() {
		return nbPages.get();
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
		nbPagesByStatus.forEach((key, value) -> result.put(key, value.get()));
		return result;
	}

	public Map<String, Long> getDocumentsByExtractor() {
		Map<String, Long> result = new HashMap<>();
		documentsByExtractor.forEach((key, value) -> result.put(key, value.get()));
		return result;
	}
}
