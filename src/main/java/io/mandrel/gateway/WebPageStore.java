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

import io.mandrel.gateway.impl.InternalStore;
import io.mandrel.http.WebPage;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hazelcast.core.HazelcastInstanceAware;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = InternalStore.class, name = "internal") })
public interface WebPageStore extends Checkable, Serializable, HazelcastInstanceAware {

	void addPage(long spiderId, String url, WebPage webPage);

	WebPage getPage(long spiderId, String url);

	void deleteAllFor(long spiderId);

	void init(Map<String, Object> properties);

	// Stream<WebPage> all(long spiderId);

	@FunctionalInterface
	public static interface Callback {
		boolean on(Collection<WebPage> elements);
	}

	void byPages(long spiderId, int pageSize, Callback callback);
}
