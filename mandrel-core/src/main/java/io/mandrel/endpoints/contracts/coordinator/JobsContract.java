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
package io.mandrel.endpoints.contracts.coordinator;

import io.mandrel.common.data.Job;
import io.mandrel.common.data.Page;
import io.mandrel.common.data.PageRequest;
import io.mandrel.endpoints.contracts.Contract;
import io.mandrel.transport.RemoteException;

import java.util.List;

import com.facebook.swift.service.ThriftException;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface JobsContract extends Contract, AutoCloseable {

	@ThriftMethod
	long fork(long id);

	@ThriftMethod
	Job get(long id);

	@ThriftMethod
	Page<Job> page(PageRequest request);

	@ThriftMethod
	Page<Job> pageForActive(PageRequest request);

	@ThriftMethod
	List<Job> listLastActive(int limit);

	@ThriftMethod
	void reinject(long jobId);

	@ThriftMethod
	void start(long jobId);

	@ThriftMethod
	void pause(long jobId);

	@ThriftMethod
	void kill(long jobId);

	@ThriftMethod
	void delete(long jobId);

	@ThriftMethod(exception = { @ThriftException(type = RemoteException.class, id = 1) })
	Job add(byte[] definition) throws RemoteException;
}
