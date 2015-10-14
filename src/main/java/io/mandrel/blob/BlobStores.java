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
package io.mandrel.blob;

import io.mandrel.common.data.Spider;

import java.util.concurrent.ConcurrentHashMap;

public class BlobStores {

	private final static ConcurrentHashMap<Long, BlobStore> stores = new ConcurrentHashMap<>();

	public static Iterable<BlobStore> list() {
		return stores.values();
	}

	public static BlobStore create(Spider spider) {
		return stores.put(spider.getId(), spider.getStores().getBlobStore());
	}
	
	public static BlobStore get(Long spiderId) {
		return stores.get(spiderId);
	}
	
	public static void remove(Long spiderId) {
		stores.remove(spiderId);
	}
}
