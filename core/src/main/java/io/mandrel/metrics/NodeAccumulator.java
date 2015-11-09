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

@Data
// TODO Use LongAdder
public class NodeAccumulator {
	private final AtomicLong nbPagesTotal = new AtomicLong(0);
	private final AtomicLong totalSizeTotal = new AtomicLong(0);
	private final Map<Integer, AtomicLong> statuses = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> hosts = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> contentTypes = new ConcurrentHashMap<>();

	private String hostname;

	public long incNbPages() {
		return nbPagesTotal.incrementAndGet();
	}

	public long incTotalSize(long size) {
		return totalSizeTotal.addAndGet(size);
	}

	public long incPageForStatus(int httpStatus) {
		AtomicLong res = statuses.get(httpStatus);
		if (res == null) {
			synchronized (statuses) {
				if (res == null) {
					res = new AtomicLong(0);
					statuses.put(httpStatus, res);
				}
			}
		}
		return res.addAndGet(1);
	}

	public long incPageForHost(String host) {
		AtomicLong res = hosts.get(host);
		if (res == null) {
			synchronized (hosts) {
				if (res == null) {
					res = new AtomicLong(0);
					hosts.put(host, res);
				}
			}
		}
		return res.addAndGet(1);
	}

	public long incPageForContentType(String contentType) {
		AtomicLong res = contentTypes.get(contentType);
		if (res == null) {
			synchronized (contentTypes) {
				if (res == null) {
					res = new AtomicLong(0);
					contentTypes.put(contentType, res);
				}
			}
		}
		return res.addAndGet(1);
	}
}
