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
package io.mandrel.transport.thrift;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.cluster.discovery.ServiceInstance;
import io.mandrel.common.ControllerNotFoundException;
import io.mandrel.common.FrontierNotFoundException;
import io.mandrel.common.WorkerNotFoundException;
import io.mandrel.endpoints.contracts.ControllerContract;
import io.mandrel.endpoints.contracts.FrontierContract;
import io.mandrel.endpoints.contracts.NodeContract;
import io.mandrel.endpoints.contracts.WorkerContract;
import io.mandrel.transport.Clients;
import io.mandrel.transport.Pooled;

import java.util.Collections;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.facebook.nifty.client.NettyClientConfig;
import com.facebook.nifty.client.NiftyClient;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.internal.compiler.CompilerThriftCodecFactory;
import com.facebook.swift.codec.metadata.ThriftCatalog;
import com.facebook.swift.service.ThriftClientManager;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

@Component
@ConditionalOnProperty(value = "transport.thrift.enabled", matchIfMissing = true)
public class ThriftClients implements Clients {

	private KeyedClientPool<FrontierContract> frontiers;
	private KeyedClientPool<ControllerContract> controllers;
	private KeyedClientPool<WorkerContract> workers;
	private KeyedClientPool<NodeContract> nodes;

	@Autowired
	private DiscoveryClient discoveryClient;

	@Value("${standalone:false}")
	private boolean local;

	@PostConstruct
	public void init() {

		GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
		poolConfig.setMaxTotalPerKey(20);
		poolConfig.setMinIdlePerKey(1);

		ThriftCatalog catalog = new ThriftCatalog();
		catalog.addDefaultCoercions(MandrelCoercions.class);
		ThriftCodecManager codecManager = new ThriftCodecManager(new CompilerThriftCodecFactory(ThriftCodecManager.class.getClassLoader()), catalog,
				Collections.emptySet());
		ThriftClientManager clientManager = new ThriftClientManager(codecManager, new NiftyClient(NettyClientConfig.newBuilder().build(), local),
				Collections.emptySet());

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
				return frontiers.get(instance.getHostAndPort());
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		} else {
			throw new FrontierNotFoundException("No frontier found");
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
				return controllers.get(instance.getHostAndPort());
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		} else {
			throw new WorkerNotFoundException("No worker found");
		}
	}

	public Pooled<WorkerContract> onWorker(HostAndPort hostAndPort) {
		return workers.get(hostAndPort);
	}

	public Pooled<WorkerContract> onRandomWorker() {
		Optional<ServiceInstance> opController = discoveryClient.getInstances(ServiceIds.worker()).stream().findFirst();
		if (opController.isPresent()) {
			try {
				ServiceInstance instance = opController.get();
				return workers.get(instance.getHostAndPort());
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		} else {
			throw new ControllerNotFoundException("No controller found");
		}
	}

	public Pooled<NodeContract> onNode(HostAndPort hostAndPort) {
		return nodes.get(hostAndPort);
	}
}
