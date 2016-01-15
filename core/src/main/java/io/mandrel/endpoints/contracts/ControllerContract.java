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
import io.mandrel.timeline.Event;

import java.util.Map;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface ControllerContract extends Contract, AutoCloseable {

	@ThriftMethod
	void addEvent(Event event);

	@ThriftMethod
	void updateMetrics(Map<String, Long> accumulators);

	public default String getServiceName() {
		return ServiceIds.controller();
	}
}
