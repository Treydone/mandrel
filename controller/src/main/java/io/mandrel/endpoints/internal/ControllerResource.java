package io.mandrel.endpoints.internal;

import io.mandrel.cluster.node.Node;
import io.mandrel.cluster.node.NodeRepository;
import io.mandrel.endpoints.contracts.ControllerContract;
import io.mandrel.metrics.MetricsRepository;
import io.mandrel.timeline.Event;
import io.mandrel.timeline.TimelineService;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ControllerResource implements ControllerContract {

	@Autowired
	private TimelineService timelineService;
	@Autowired
	private MetricsRepository metricsRepository;
	@Autowired
	private NodeRepository nodeRepository;

	@Override
	public void close() throws Exception {

	}

	@Override
	public void addEvent(Event event) {
		timelineService.add(event);
	}

	@Override
	public void updateMetrics(Map<String, Long> accumulators) {
		metricsRepository.sync(accumulators);
	}

	@Override
	public void updateNode(Node node) {
		nodeRepository.update(Arrays.asList(node));
	}
}
