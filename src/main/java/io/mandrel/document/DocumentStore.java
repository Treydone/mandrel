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

import io.mandrel.common.loader.NamedComponent;
import io.mandrel.data.content.MetadataExtractor;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.hazelcast.core.HazelcastInstanceAware;

public interface DocumentStore extends NamedComponent, Checkable, Serializable, HazelcastInstanceAware {

	void init(MetadataExtractor webPageExtractor);

	void save(long spiderId, Document document);

	void save(long spiderId, List<Document> documents);

	void deleteAllFor(long spiderId);

	// Stream<Document> all(long spiderId);

	@FunctionalInterface
	public static interface Callback {
		boolean on(Collection<Document> elements);
	}

	void byPages(long spiderId, int pageSize, Callback callback);

	Collection<Document> byPages(long spiderId, int pageSize, int pageNumber);

	long total(long spiderId);
}
