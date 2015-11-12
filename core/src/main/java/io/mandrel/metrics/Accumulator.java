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
