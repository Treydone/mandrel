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

import io.mandrel.common.loader.NamedComponent;
import io.mandrel.data.spider.Link;
import io.mandrel.frontier.Politeness;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import com.hazelcast.core.HazelcastInstanceAware;

public interface MetadataStore extends NamedComponent, Checkable, Serializable, HazelcastInstanceAware {

	void init(Map<String, Object> properties);

	void addMetadata(long spiderId, URI uri, FetchMetadata metadata);

	FetchMetadata getMetadata(long spiderId, URI uri);

	Set<Link> filter(long spiderId, Set<Link> outlinks, Politeness politeness);

	void deleteAllFor(long spiderId);
}
