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
package io.mandrel.endpoints.contracts;

import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.common.net.Uri;
import io.mandrel.common.sync.Container;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;

import java.util.List;
import java.util.Set;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import com.google.common.util.concurrent.ListenableFuture;

@ThriftService
public interface FrontierContract extends Contract, AutoCloseable {

	@ThriftMethod
	SyncResponse sync(@ThriftField(value = 1, name = "sync") SyncRequest sync);

	@ThriftMethod
	List<Container> listRunningContainers();

	@ThriftMethod
	void create(@ThriftField(value = 1, name = "definition") byte[] definition);

	@ThriftMethod
	void start(@ThriftField(value = 1, name = "id") Long id);

	@ThriftMethod
	void pause(@ThriftField(value = 1, name = "id") Long id);

	@ThriftMethod
	void kill(@ThriftField(value = 1, name = "id") Long id);

	@ThriftMethod
	ListenableFuture<Uri> next(@ThriftField(value = 1, name = "id") Long id);

	@ThriftMethod
	void delete(@ThriftField(value = 1, name = "id") Long id, @ThriftField(value = 2, name = "uri") Uri uri);

	@ThriftMethod
	void schedule(@ThriftField(value = 1, name = "id") Long id, @ThriftField(value = 2, name = "uri") Uri uri);

	@ThriftMethod
	void mschedule(@ThriftField(value = 1, name = "id") Long id, @ThriftField(value = 2, name = "uris") Set<Uri> uris);

	public default String getServiceName() {
		return ServiceIds.frontier();
	}
}
