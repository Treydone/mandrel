package io.mandrel.stats;

import java.util.HashMap;
import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

public class Stats {

	private final IAtomicLong nbPages;
	private final IAtomicLong totalSize;
	private final Map<Integer, IAtomicLong> nbPagesByStatus = new HashMap<>();
	private final long spiderId;

	private final transient HazelcastInstance instance;

	public Stats(HazelcastInstance instance, long spiderId) {
		this.instance = instance;
		this.spiderId = spiderId;

		nbPages = instance.getAtomicLong(getKey(spiderId) + "-nbPages");
		totalSize = instance.getAtomicLong(getKey(spiderId) + "-totalSize");
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

	public long incPageForStatus(int httpStatus) {
		IAtomicLong iAtomicLong = nbPagesByStatus.get(httpStatus);
		if (iAtomicLong == null) {
			iAtomicLong = instance.getAtomicLong(getKey(spiderId) + "-status-" + httpStatus);
			nbPagesByStatus.put(httpStatus, iAtomicLong);
		}
		return iAtomicLong.incrementAndGet();
	}

	public long getNbPages() {
		return nbPages.get();
	}

	public long getTotalSize() {
		return totalSize.get();
	}

	public Map<Integer, Long> getPagesByStatus() {
		Map<Integer, Long> result = new HashMap<>();
		nbPagesByStatus.forEach((key, value) -> result.put(key, value.get()));
		return result;
	}
}
