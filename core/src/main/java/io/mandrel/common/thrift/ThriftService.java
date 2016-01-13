package io.mandrel.common.thrift;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.ServiceInstance;
import io.mandrel.endpoints.contracts.Contract;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.facebook.nifty.core.NiftyTimer;
import com.facebook.nifty.processor.NiftyProcessor;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.service.ThriftServiceProcessor;
import com.facebook.swift.service.ThriftServiceStatsHandler;

@Component
@Slf4j
public class ThriftService {

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private List<? extends Contract> resources;

	@Autowired
	private ThriftServerProperties properties;

	@Value("${standalone:false}")
	private boolean local;

	private ThriftServer server;

	@PostConstruct
	public void init() {

		resources.forEach(resource -> {
			log.debug("Registering service {}", resource.getServiceName());
			ServiceInstance instance = ServiceInstance.builder().port(properties.getPort()).name(resource.getServiceName()).build();
			discoveryClient.register(instance);
		});

		NiftyProcessor processor = new ThriftServiceProcessor(new ThriftCodecManager(), Arrays.asList(new ThriftServiceStatsHandler()), resources);

		server = new ThriftServer(processor, properties, new NiftyTimer("thrift"), ThriftServer.DEFAULT_FRAME_CODEC_FACTORIES,
				ThriftServer.DEFAULT_PROTOCOL_FACTORIES, ThriftServer.DEFAULT_WORKER_EXECUTORS, ThriftServer.DEFAULT_SECURITY_FACTORY, local);
		server.start();
	}

	@PreDestroy
	public void destroy() {

		resources.forEach(resource -> {
			log.debug("Registering service {}", resource.getServiceName());
			ServiceInstance instance = ServiceInstance.builder().port(properties.getPort()).name(resource.getServiceName()).build();
			discoveryClient.unregister(instance);
		});

		server.close();
	}
}
