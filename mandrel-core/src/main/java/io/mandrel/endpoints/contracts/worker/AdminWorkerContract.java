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
package io.mandrel.endpoints.contracts.worker;

import io.mandrel.common.sync.Container;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;
import io.mandrel.endpoints.contracts.Contract;

import java.util.List;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface AdminWorkerContract extends Contract, AutoCloseable {

	@ThriftMethod
	SyncResponse syncWorkers(@ThriftField(value = 1, name = "sync") SyncRequest sync);

	@ThriftMethod
	List<Container> listRunningWorkerContainers();

	@ThriftMethod
	void createWorkerContainer(@ThriftField(value = 1, name = "definition") byte[] definition);

	@ThriftMethod
	void startWorkerContainer(@ThriftField(value = 1, name = "id") Long id);

	@ThriftMethod
	void pauseWorkerContainer(@ThriftField(value = 1, name = "id") Long id);

	@ThriftMethod
	void killWorkerContainer(@ThriftField(value = 1, name = "id") Long id);

}
