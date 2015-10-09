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

import io.mandrel.blob.impl.BlobInternalStore;
import io.mandrel.metadata.FetchMetadata;
import io.mandrel.monitor.health.Checkable;
import io.mandrel.requests.Bag;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hazelcast.core.HazelcastInstanceAware;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = BlobInternalStore.class, name = "internal") })
public interface BlobStore extends Checkable, Serializable, HazelcastInstanceAware {

	String getType();

	void addBag(long spiderId, String url, Bag<? extends FetchMetadata> bag);

	Bag<? extends FetchMetadata> getBag(long spiderId, String url);

	void deleteAllFor(long spiderId);

	void init(Map<String, Object> properties);

	// Stream<WebPage> all(long spiderId);

	@FunctionalInterface
	public static interface Callback {
		boolean on(Collection<Bag<? extends FetchMetadata>> elements);
	}

	void byPages(long spiderId, int pageSize, Callback callback);
}
