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
package io.mandrel.common.thrift;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.cluster.discovery.ServiceInstance;
import io.mandrel.common.ControllerNotFoundException;
import io.mandrel.endpoints.contracts.ControllerContract;
import io.mandrel.endpoints.contracts.FrontierContract;
import io.mandrel.endpoints.contracts.NodeContract;
import io.mandrel.endpoints.contracts.WorkerContract;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.facebook.swift.service.ThriftClientManager;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

@Component
public class Clients {

	private KeyedClientPool<FrontierContract> frontiers;
	private KeyedClientPool<ControllerContract> controllers;
	private KeyedClientPool<WorkerContract> workers;
	private KeyedClientPool<NodeContract> nodes;

	@Autowired
	private DiscoveryClient discoveryClient;

	@PostConstruct
	public void init() {

		GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
		poolConfig.setMaxTotalPerKey(20);
		poolConfig.setMinIdlePerKey(1);

		ThriftClientManager clientManager = new ThriftClientManager();

		frontiers = new KeyedClientPool<>(FrontierContract.class, poolConfig, 9090,
		// Deflater.BEST_SPEED
				null, clientManager);
		controllers = new KeyedClientPool<>(ControllerContract.class, poolConfig, 9090,
		// Deflater.BEST_SPEED
				null, clientManager);
		workers = new KeyedClientPool<>(WorkerContract.class, poolConfig, 9090,
		// Deflater.BEST_SPEED
				null, clientManager);
		nodes = new KeyedClientPool<>(NodeContract.class, poolConfig, 9090,
		// Deflater.BEST_SPEED
				null, clientManager);
	}

	public Pooled<FrontierContract> onFrontier(HostAndPort hostAndPort) {
		return frontiers.get(hostAndPort);
	}

	public Pooled<FrontierContract> onRandomFrontier() {
		Optional<ServiceInstance> opController = discoveryClient.getInstances(ServiceIds.frontier()).stream().findFirst();
		if (opController.isPresent()) {
			try {
				ServiceInstance instance = opController.get();
				return frontiers.get(HostAndPort.fromParts(instance.getHost(), instance.getPort()));
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		} else {
			throw new ControllerNotFoundException("No frontier found");
		}
	}

	public Pooled<ControllerContract> onController(HostAndPort hostAndPort) {
		return controllers.get(hostAndPort);
	}

	public Pooled<ControllerContract> onRandomController() {
		Optional<ServiceInstance> opController = discoveryClient.getInstances(ServiceIds.controller()).stream().findFirst();
		if (opController.isPresent()) {
			try {
				ServiceInstance instance = opController.get();
				return controllers.get(HostAndPort.fromParts(instance.getHost(), instance.getPort()));
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		} else {
			throw new ControllerNotFoundException("No controller found");
		}
	}

	public Pooled<WorkerContract> onWorker(HostAndPort hostAndPort) {
		return workers.get(hostAndPort);
	}

	public Pooled<NodeContract> onNode(HostAndPort hostAndPort) {
		return nodes.get(hostAndPort);
	}
}
