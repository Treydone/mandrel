package io.mandrel.cluster.instance;

import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.cluster.node.Node;
import io.mandrel.common.client.Clients;
import io.mandrel.timeline.NodeEvent;
import io.mandrel.timeline.NodeEvent.NodeEventType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StateService implements ApplicationListener<ContextStartedEvent> {

	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private Clients clients;

	private final AtomicBoolean started = new AtomicBoolean();

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {
		started.set(true);
		Optional<ServiceInstance> controller = discoveryClient.getInstances(ServiceIds.CONTROLLER).stream().findFirst();
		if (controller.isPresent()) {
			clients.controllerClient().add(
					new NodeEvent().setNodeId(Node.idOf(discoveryClient.getLocalServiceInstance().getUri())).setType(NodeEventType.NODE_STARTED)
							.setTime(LocalDateTime.now()), controller.get().getUri());
		} else {
			log.warn("No controller found");
		}
	}

	public boolean isStarted() {
		return started.get();
	}

	@PreDestroy
	public void destroy() {
		Optional<ServiceInstance> controller = discoveryClient.getInstances(ServiceIds.CONTROLLER).stream().findFirst();
		if (controller.isPresent()) {
			clients.controllerClient().add(
					new NodeEvent().setNodeId(Node.idOf(discoveryClient.getLocalServiceInstance().getUri())).setType(NodeEventType.NODE_STOPPED)
							.setTime(LocalDateTime.now()), controller.get().getUri());
		} else {
			log.warn("No controller found");
		}
	}
}
