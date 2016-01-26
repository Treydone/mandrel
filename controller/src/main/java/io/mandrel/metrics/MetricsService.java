package io.mandrel.metrics;

import java.util.Map;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MetricsService {

	private final MetricsRepository metricsRepository;

	public void sync(Map<String, Long> accumulators) {
		metricsRepository.sync(accumulators);
	}

	public Timeserie serie(String name) {
		return metricsRepository.serie(name);
	}

	public NodeMetrics node(String nodeId) {
		return metricsRepository.node(nodeId);
	}

	public GlobalMetrics global() {
		return metricsRepository.global();

	}

	public SpiderMetrics spider(long spiderId) {
		return metricsRepository.spider(spiderId);
	}

	public void delete(long spiderId) {
		metricsRepository.delete(spiderId);
	}

}
