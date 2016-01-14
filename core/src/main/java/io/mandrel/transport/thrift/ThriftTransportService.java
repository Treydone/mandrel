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
import io.mandrel.cluster.discovery.ServiceInstance;
import io.mandrel.endpoints.contracts.Contract;
import io.mandrel.timeline.Event;
import io.mandrel.timeline.Event.NodeInfo.NodeEventType;
import io.mandrel.transport.Clients;
import io.mandrel.transport.TransportProperties;
import io.mandrel.transport.TransportService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.facebook.nifty.core.NiftyTimer;
import com.facebook.nifty.processor.NiftyProcessor;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.internal.compiler.CompilerThriftCodecFactory;
import com.facebook.swift.codec.metadata.ThriftCatalog;
import com.facebook.swift.service.ThriftServiceProcessor;
import com.facebook.swift.service.ThriftServiceStatsHandler;

@Component
@Slf4j
@ConditionalOnProperty(value = "transport.thrift.enabled", matchIfMissing = true)
public class ThriftTransportService implements TransportService {

	@Autowired
	private Clients clients;
	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private List<? extends Contract> resources;
	@Autowired
	private ThriftTransportProperties properties;
	@Autowired
	private TransportProperties globalProperties;

	@Value("${standalone:false}")
	private boolean local;

	private ThriftServer server;

	@PostConstruct
	public void init() {
		ThriftCatalog catalog = new ThriftCatalog();
		catalog.addDefaultCoercions(MandrelCoercions.class);
		ThriftCodecManager codecManager = new ThriftCodecManager(new CompilerThriftCodecFactory(ThriftCodecManager.class.getClassLoader()), catalog,
				Collections.emptySet());

		NiftyProcessor processor = new ThriftServiceProcessor(codecManager, Arrays.asList(new ThriftServiceStatsHandler()), resources);

		properties.setPort(globalProperties.getPort());
		properties.setBindAddress(globalProperties.getBindAddress());

		server = new ThriftServer(processor, properties, new NiftyTimer("thrift"), ThriftServer.DEFAULT_FRAME_CODEC_FACTORIES,
				ThriftServer.DEFAULT_PROTOCOL_FACTORIES, ThriftServer.DEFAULT_WORKER_EXECUTORS, ThriftServer.DEFAULT_SECURITY_FACTORY, local);
		server.start();

		resources.forEach(resource -> {
			log.debug("Registering service {}", resource.getServiceName());
			ServiceInstance instance = ServiceInstance.builder().port(globalProperties.getPort()).name(resource.getServiceName()).build();
			discoveryClient.register(instance);
		});

		Event event = Event.forNode();
		event.getNode().setNodeId(discoveryClient.getInstanceId()).setType(NodeEventType.NODE_STARTED);
		send(event);
	}

	@PreDestroy
	public void destroy() {

		log.debug("Shutting down thrift transport");

		resources.forEach(resource -> {
			log.debug("Unregistering service {}", resource.getServiceName());
			discoveryClient.unregister(resource.getServiceName());
		});

		server.close();

		Event event = Event.forNode();
		event.getNode().setNodeId(discoveryClient.getInstanceId()).setType(NodeEventType.NODE_STOPPED);
		send(event);
	}

	public void send(Event event) {
		try {
			clients.onRandomController().with(service -> service.addEvent(event));
		} catch (Exception e) {
			log.warn("Can not send event", e);
		}
	}
}
