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
package io.mandrel.command;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.ServiceInstance;

import java.util.List;

public class Runner {

	@FunctionalInterface
	public static interface Action {
		public void on(ServiceInstance i);
	}

	public static void runOnAllInstaces(DiscoveryClient discoveryClient, String serviceId, Action action) {
		List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
		instances.forEach(i -> {
			action.on(i);
		});
	}

}
