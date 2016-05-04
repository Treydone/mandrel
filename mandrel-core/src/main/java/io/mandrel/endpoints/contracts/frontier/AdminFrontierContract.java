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
package io.mandrel.endpoints.contracts.frontier;

import io.mandrel.common.sync.Container;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;
import io.mandrel.endpoints.contracts.Contract;
import io.mandrel.transport.RemoteException;

import java.util.List;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftException;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface AdminFrontierContract extends Contract, AutoCloseable {

	@ThriftMethod(exception = { @ThriftException(type = RemoteException.class, id = 1) })
	SyncResponse syncFrontiers(@ThriftField(value = 1, name = "sync") SyncRequest sync) throws RemoteException;

	@ThriftMethod(exception = { @ThriftException(type = RemoteException.class, id = 1) })
	List<Container> listRunningFrontierContainers() throws RemoteException;

	@ThriftMethod(exception = { @ThriftException(type = RemoteException.class, id = 1) })
	void createFrontierContainer(@ThriftField(value = 1, name = "definition") byte[] definition) throws RemoteException;

	@ThriftMethod(exception = { @ThriftException(type = RemoteException.class, id = 1) })
	void startFrontierContainer(@ThriftField(value = 1, name = "id") Long id) throws RemoteException;

	@ThriftMethod(exception = { @ThriftException(type = RemoteException.class, id = 1) })
	void pauseFrontierContainer(@ThriftField(value = 1, name = "id") Long id) throws RemoteException;

	@ThriftMethod(exception = { @ThriftException(type = RemoteException.class, id = 1) })
	void killFrontierContainer(@ThriftField(value = 1, name = "id") Long id) throws RemoteException;

}
