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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class SpiderAccumulator {

	private final AtomicLong pendings = new AtomicLong(0);
	private final AtomicLong nbPages = new AtomicLong(0);
	private final AtomicLong totalSize = new AtomicLong(0);
	private final AtomicLong totalTimeToFetch = new AtomicLong(0);

	private final AtomicLong readTimeout = new AtomicLong(0);
	private final AtomicLong connectTimeout = new AtomicLong(0);
	private final AtomicLong connectException = new AtomicLong(0);

	private final Map<String, AtomicLong> extractors = new ConcurrentHashMap<>();
	private final Map<Integer, AtomicLong> statuses = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> hosts = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> contentTypes = new ConcurrentHashMap<>();

	private final long spiderd;

	public SpiderAccumulator(long spiderd) {
		this.spiderd = spiderd;
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

	public long incPendings() {
		return pendings.incrementAndGet();
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

	public long incDocumentForExtractor(String extractor, int number) {
		AtomicLong res = extractors().get(extractor);
		if (res == null) {
			synchronized (extractors()) {
				if (res == null) {
					res = new AtomicLong(0);
					extractors().put(extractor, res);
				}
			}
		}
		return res.addAndGet(number);
	}
}
