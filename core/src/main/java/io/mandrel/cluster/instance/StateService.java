package io.mandrel.cluster.instance;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.node.Node;
import io.mandrel.common.net.Uri;
import io.mandrel.common.thrift.Clients;
import io.mandrel.endpoints.contracts.Contract;
import io.mandrel.timeline.Event;
import io.mandrel.timeline.Event.NodeInfo.NodeEventType;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

@Component
public class StateService implements ApplicationListener<ContextStartedEvent> {

	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private Clients clients;
	@Autowired
	private List<? extends Contract> resources;

	private final AtomicBoolean started = new AtomicBoolean();

	@Override
	public void onApplicationEvent(ContextStartedEvent contextStartedEvent) {
		started.set(true);

		resources.forEach(resource -> {
			Event event = Event.forNode();
			event.getNode().setNodeId(Node.idOf(Uri.internal(si.getHost(), si.getPort())))
					.setType(NodeEventType.NODE_STARTED);
			send(event);
		});
	}

	public boolean isStarted() {
		return started.get();
	}

	@PreDestroy
	public void destroy() {
		Event event = Event.forNode();
		event.getNode().setNodeId(Node.idOf(Uri.create(discoveryClient.getLocalServiceInstance().getUri()))).setType(NodeEventType.NODE_STOPPED);
		send(event);

		started.set(false);
	}

	public void send(Event event) {
		clients.onRandomController().with(service -> service.addEvent(event));
	}
}
