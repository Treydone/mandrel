package io.mandrel.stats;

import java.util.HashMap;
import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

public class Stats {

	private final IAtomicLong nbPages;
	private final IAtomicLong nbPendingPages;
	private final IAtomicLong totalSize;
	private final IAtomicLong totalTimeToFetch;
	private final Map<Integer, IAtomicLong> nbPagesByStatus = new HashMap<>();
	private final long spiderId;

	private final transient HazelcastInstance instance;

	public Stats(HazelcastInstance instance, long spiderId) {
		this.instance = instance;
		this.spiderId = spiderId;

		nbPages = instance.getAtomicLong(getKey(spiderId) + "-nbPages");
		nbPendingPages = instance.getAtomicLong(getKey(spiderId) + "-nbPendingPages");
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

	public long incNbPendingPages() {
		return nbPendingPages.incrementAndGet();
	}

	public long decNbPendingPages() {
		return nbPendingPages.incrementAndGet();
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
		return iAtomicLong.incrementAndGet();
	}

	public long getNbPendingPages() {
		return nbPendingPages.get();
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
}
