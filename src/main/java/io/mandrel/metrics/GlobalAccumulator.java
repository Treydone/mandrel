package io.mandrel.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class GlobalAccumulator {
	private final AtomicLong nbPagesTotal = new AtomicLong(0);
	private final AtomicLong totalSizeTotal = new AtomicLong(0);
	private final Map<Integer, AtomicLong> statuses = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> hosts = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> contentTypes = new ConcurrentHashMap<>();

	public long incNbPages() {
		return nbPagesTotal().incrementAndGet();
	}

	public long incTotalSize(long size) {
		return totalSizeTotal().addAndGet(size);
	}

	public long incPageForStatus(int httpStatus) {
		AtomicLong res = statuses().get(httpStatus);
		if (res == null) {
			synchronized (statuses()) {
				if (res == null) {
					res = new AtomicLong(0);
					statuses().put(httpStatus, res);
				}
			}
		}
		return res.addAndGet(1);
	}

	public long incPageForHost(String host) {
		AtomicLong res = hosts().get(host);
		if (res == null) {
			synchronized (hosts()) {
				if (res == null) {
					res = new AtomicLong(0);
					hosts().put(host, res);
				}
			}
		}
		return res.addAndGet(1);
	}

	public long incPageForContentType(String contentType) {
		AtomicLong res = contentTypes().get(contentType);
		if (res == null) {
			synchronized (contentTypes()) {
				if (res == null) {
					res = new AtomicLong(0);
					contentTypes().put(contentType, res);
				}
			}
		}
		return res.addAndGet(1);
	}
}
