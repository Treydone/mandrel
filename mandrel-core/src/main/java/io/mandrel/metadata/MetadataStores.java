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
package io.mandrel.metadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataStores {

	private final static Map<Long, MetadataStore> stores = new HashMap<>();

	public static Iterable<MetadataStore> list() {
		return stores.values();
	}

	public static void add(long jobId, MetadataStore metadataStore) {
		synchronized (stores) {
			MetadataStore oldMetadataStore = stores.put(jobId, metadataStore);
			if (oldMetadataStore != null) {
				try {
					oldMetadataStore.close();
				} catch (IOException e) {
					log.warn("Can not close", e);
				}
			}
		}
	}

	public static MetadataStore get(Long jobId) {
		return stores.get(jobId);
	}

	public static void remove(Long jobId) {
		synchronized (stores) {
			MetadataStore oldMetadataStore = stores.remove(jobId);
			if (oldMetadataStore != null) {
				try {
					oldMetadataStore.close();
				} catch (IOException e) {
					log.warn("Can not close", e);
				}
			}
		}
	}
}
