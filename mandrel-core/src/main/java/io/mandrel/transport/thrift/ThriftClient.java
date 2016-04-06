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

import io.airlift.units.Duration;
import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.endpoints.contracts.Contract;
import io.mandrel.endpoints.contracts.NodeContract;
import io.mandrel.endpoints.contracts.coordinator.AdminCoordinatorContract;
import io.mandrel.endpoints.contracts.coordinator.JobsContract;
import io.mandrel.endpoints.contracts.coordinator.MetricsContract;
import io.mandrel.endpoints.contracts.coordinator.NodesContract;
import io.mandrel.endpoints.contracts.coordinator.TimelineContract;
import io.mandrel.endpoints.contracts.frontier.AdminFrontierContract;
import io.mandrel.endpoints.contracts.frontier.FrontierContract;
import io.mandrel.endpoints.contracts.worker.AdminWorkerContract;
import io.mandrel.endpoints.contracts.worker.WorkerContract;
import io.mandrel.transport.MandrelClient;
import io.mandrel.transport.Targeted;
import io.mandrel.transport.TransportProperties;
import io.mandrel.transport.thrift.nifty.NiftyClient;
import io.mandrel.transport.thrift.nifty.ThriftClientManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.facebook.nifty.client.NettyClientConfig;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.internal.compiler.CompilerThriftCodecFactory;
import com.facebook.swift.codec.metadata.ThriftCatalog;

@Component
@ConditionalOnProperty(value = "transport.thrift.enabled", matchIfMissing = true)
public class ThriftClient implements MandrelClient {

	private Map<Class<? extends Contract>, KeyedClientPool<? extends Contract>> contracts;

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private TransportProperties transportProperties;

	@PostConstruct
	public void init() {

		GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
		poolConfig.setMaxTotalPerKey(4);
		poolConfig.setMinIdlePerKey(1);

		ThriftCatalog catalog = new ThriftCatalog();
		catalog.addDefaultCoercions(MandrelCoercions.class);
		ThriftCodecManager codecManager = new ThriftCodecManager(new CompilerThriftCodecFactory(ThriftCodecManager.class.getClassLoader()), catalog,
				Collections.emptySet());

		NettyClientConfig config = NettyClientConfig.newBuilder().build();
		NiftyClient niftyClient = new NiftyClient(config, transportProperties.isLocal());
		ThriftClientManager clientManager = new ThriftClientManager(codecManager, niftyClient, Collections.emptySet());

		contracts = Arrays.asList(
		// Frontier
				FrontierContract.class, AdminFrontierContract.class,

				// Coordinator
				TimelineContract.class, JobsContract.class, MetricsContract.class, NodesContract.class, AdminCoordinatorContract.class,

				// Worker
				WorkerContract.class, AdminWorkerContract.class,

				// Common
				NodeContract.class).stream()
				.map(clazz -> Pair.of(clazz, prepare(new KeyedClientPool<>(clazz, poolConfig, 9090, null, clientManager, transportProperties.isLocal()))))
				.collect(Collectors.toMap(pair -> pair.getLeft(), pair -> pair.getRight()));
	}

	protected KeyedClientPool<?> prepare(KeyedClientPool<?> pool) {
		pool.setConnectTimeout(Duration.valueOf(transportProperties.getConnectTimeout().toString()));
		pool.setReadTimeout(Duration.valueOf(transportProperties.getReadTimeout().toString()));
		pool.setReceiveTimeout(Duration.valueOf(transportProperties.getReceiveTimeout().toString()));
		pool.setWriteTimeout(Duration.valueOf(transportProperties.getWriteTimeout().toString()));
		pool.setMaxFrameSize(transportProperties.getMaxFrameSize());
		return pool;
	}

	protected <T extends Contract & AutoCloseable> KeyedClientPool<T> get(Class<T> clazz) {
		return (KeyedClientPool<T>) contracts.get(clazz);
	}

	@Override
	public DiscoveryClient discovery() {
		return discoveryClient;
	}

	@Override
	public Targeted<NodeContract> node() {
		return new Targeted<>(discoveryClient, ServiceIds.node(), get(NodeContract.class));
	}

	@Override
	public FrontierClient frontier() {
		return new FrontierClient() {

			@Override
			public Targeted<FrontierContract> client() {
				return new Targeted<>(discoveryClient, ServiceIds.frontier(), get(FrontierContract.class));
			}

			@Override
			public Targeted<AdminFrontierContract> admin() {
				return new Targeted<>(discoveryClient, ServiceIds.frontier(), get(AdminFrontierContract.class));
			}
		};
	}

	@Override
	public CoordinatorClient coordinator() {
		return new CoordinatorClient() {

			@Override
			public Targeted<TimelineContract> events() {
				return new Targeted<>(discoveryClient, ServiceIds.coordinator(), get(TimelineContract.class));
			}

			@Override
			public Targeted<JobsContract> jobs() {
				return new Targeted<>(discoveryClient, ServiceIds.coordinator(), get(JobsContract.class));
			}

			@Override
			public Targeted<MetricsContract> metrics() {
				return new Targeted<>(discoveryClient, ServiceIds.coordinator(), get(MetricsContract.class));
			}

			@Override
			public Targeted<NodesContract> nodes() {
				return new Targeted<>(discoveryClient, ServiceIds.coordinator(), get(NodesContract.class));
			}

			@Override
			public Targeted<AdminCoordinatorContract> admin() {
				return new Targeted<>(discoveryClient, ServiceIds.coordinator(), get(AdminCoordinatorContract.class));
			}
		};
	}

	@Override
	public WorkerClient worker() {
		return new WorkerClient() {

			@Override
			public Targeted<WorkerContract> client() {
				return new Targeted<>(discoveryClient, ServiceIds.worker(), get(WorkerContract.class));
			}

			@Override
			public Targeted<AdminWorkerContract> admin() {
				return new Targeted<>(discoveryClient, ServiceIds.worker(), get(AdminWorkerContract.class));
			}
		};
	}
}
