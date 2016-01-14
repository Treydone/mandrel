package io.mandrel.transport.thrift;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.ServiceInstance;
import io.mandrel.cluster.node.Node;
import io.mandrel.common.net.Uri;
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
		event.getNode().setNodeId(Node.idOf(Uri.internal(discoveryClient.getInstanceHost(), globalProperties.getPort())));
		event.getNode().setType(NodeEventType.NODE_STARTED);
		send(event);
	}

	@PreDestroy
	public void destroy() {

		resources.forEach(resource -> {
			log.debug("Registering service {}", resource.getServiceName());
			ServiceInstance instance = ServiceInstance.builder().port(globalProperties.getPort()).name(resource.getServiceName()).build();
			discoveryClient.unregister(instance);
		});

		server.close();

		Event event = Event.forNode();
		event.getNode().setNodeId(Node.idOf(Uri.internal(discoveryClient.getInstanceHost(), globalProperties.getPort())));
		event.getNode().setType(NodeEventType.NODE_STOPPED);
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
