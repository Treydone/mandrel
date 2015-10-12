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
package io.mandrel.blob.impl;

import io.mandrel.blob.Blob;
import io.mandrel.blob.BlobStore;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.util.IterationType;

@Data
public class BlobInternalStore implements BlobStore {

	private static final long serialVersionUID = -775049235484042261L;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	protected transient HazelcastInstance hazelcastInstance;

	public BlobInternalStore() {
	}

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void init(Map<String, Object> properties) {

	}

	@Override
	public void putBlob(long spiderId, URI uri, Blob blob) {
		getBlob(spiderId).set(uri, blob);
	}

	@Override
	public Blob getBlob(long spiderId, URI uri) {
		return getBlob(spiderId).get(uri);
	}

	@Override
	public void deleteAllFor(long spiderId) {
		getBlob(spiderId).destroy();
	}

	@Override
	public void byPages(long spiderId, int pageSize, Callback callback) {
		PagingPredicate predicate = new PagingPredicate(pageSize);
		predicate.setIterationType(IterationType.VALUE);

		boolean loop = true;
		while (loop) {
			Collection<Blob> values = getBlob(spiderId).values(predicate);
			loop = callback.on(values);
			predicate.nextPage();
		}
	}

	public IMap<URI, Blob> getBlob(long spiderId) {
		return hazelcastInstance.<URI, Blob> getMap("blob-" + spiderId);
	}

	@Override
	public String name() {
		return "internal";
	}
}
