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
package io.mandrel.document;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DocumentStores {

	private final static Map<Long, Map<String, DocumentStore>> stores = new HashMap<>();

	public static void add(long spiderId, String name, DocumentStore documentStore) {
		synchronized (stores) {
			Map<String, io.mandrel.document.DocumentStore> map = stores.get(spiderId);
			if (map == null) {
				map = new HashMap<>();
				stores.put(spiderId, map);
			}
			map.put(name, documentStore);
		}
	}

	public static Optional<Map<String, DocumentStore>> get(Long spiderId) {
		return stores.get(spiderId) != null ? Optional.of(stores.get(spiderId)) : Optional.empty();
	}

	public static Optional<DocumentStore> get(Long spiderId, String name) {
		return stores.get(spiderId) != null ? (stores.get(spiderId).get(name) != null ? Optional.of(stores.get(spiderId).get(name)) : Optional.empty())
				: Optional.empty();
	}

	public static void remove(Long spiderId) {
		synchronized (stores) {
			stores.remove(spiderId);
		}
	}
}
