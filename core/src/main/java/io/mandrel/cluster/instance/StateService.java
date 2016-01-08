package io.mandrel.cluster.instance;

import io.mandrel.cluster.node.Node;
import io.mandrel.common.net.Uri;
import io.mandrel.common.thrift.Clients;
import io.mandrel.timeline.Event;
import io.mandrel.timeline.Event.NodeInfo.NodeEventType;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

@Component
public class StateService implements ApplicationListener<ContextStartedEvent> {

	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private Clients clients;

	private final AtomicBoolean started = new AtomicBoolean();

	@Override
	public void onApplicationEvent(ContextStartedEvent contextStartedEvent) {
		started.set(true);

		Event event = Event.forNode();
		event.getNode().setNodeId(Node.idOf(Uri.create(discoveryClient.getLocalServiceInstance().getUri()))).setType(NodeEventType.NODE_STARTED);
		send(event);
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
