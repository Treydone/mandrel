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
import io.mandrel.common.service.TaskContext;

import java.net.URI;
import java.util.Collection;

import lombok.Data;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.util.IterationType;

public class InternalBlobStore extends BlobStore {

	@Data
	public static class InternalBlobStoreDefinition implements BlobStoreDefinition {

		private static final long serialVersionUID = -9205125497698919267L;

		@Override
		public String name() {
			return "internal";
		}

		@Override
		public BlobStore build(TaskContext context) {
			return new InternalBlobStore(context);
		}
	}

	private final HazelcastInstance hazelcastInstance;

	public InternalBlobStore(TaskContext context) {
		super(context);
		hazelcastInstance = context.getInstance();
	}

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void init() {

	}

	@Override
	public void putBlob(URI uri, Blob blob) {
		getBlob().set(uri, blob);
	}

	@Override
	public Blob getBlob(URI uri) {
		return getBlob().get(uri);
	}

	@Override
	public void deleteAll() {
		getBlob().destroy();
	}

	@Override
	public void byPages(int pageSize, Callback callback) {
		PagingPredicate predicate = new PagingPredicate(pageSize);
		predicate.setIterationType(IterationType.VALUE);

		boolean loop = true;
		while (loop) {
			Collection<Blob> values = getBlob().values(predicate);
			loop = callback.on(values);
			predicate.nextPage();
		}
	}

	public IMap<URI, Blob> getBlob() {
		return hazelcastInstance.<URI, Blob> getMap("blob-" + context.getSpiderId());
	}
}
