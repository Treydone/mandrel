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
package io.mandrel.gateway;

import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.gateway.impl.CassandraDocumentStore;
import io.mandrel.gateway.impl.InternalDocumentStore;
import io.mandrel.gateway.impl.JdbcDocumentStore;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hazelcast.core.HazelcastInstanceAware;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = InternalDocumentStore.class, name = "internal"), @Type(value = JdbcDocumentStore.class, name = "jdbc"),
		@Type(value = CassandraDocumentStore.class, name = "cassandra") })
public interface DocumentStore extends Checkable, Serializable, HazelcastInstanceAware {

	String getType();

	void init(WebPageExtractor webPageExtractor);

	void save(long spiderId, Document doc);

	void save(long spiderId, List<Document> data);

	void deleteAllFor(long spiderId);

	// Stream<Document> all(long spiderId);

	@FunctionalInterface
	public static interface Callback {
		boolean on(Collection<Document> elements);
	}

	void byPages(long spiderId, int pageSize, Callback callback);

	Collection<Document> byPages(long spiderId, int pageSize);

}
