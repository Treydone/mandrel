package io.mandrel.metrics;

import io.mandrel.metrics.Timeserie.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.LongStream;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MetricsService {

	private final MetricsRepository metricsRepository;

	public void sync(Map<String, Long> accumulators) {
		metricsRepository.sync(accumulators);
	}

	public Timeserie serie(String name) {
		Timeserie serie = metricsRepository.serie(name);

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime minus4Hours = now.withMinute(0).withSecond(0).withNano(0).minusHours(4);

		LocalDateTime firstTime = CollectionUtils.isNotEmpty(serie) && serie.first() != null && serie.first().getTime().isBefore(minus4Hours) ? serie.first()
				.getTime() : minus4Hours;
		LocalDateTime lastTime = now;

		Set<Data> results = LongStream.range(0, Duration.between(firstTime, lastTime).toMinutes()).mapToObj(minutes -> firstTime.plusMinutes(minutes))
				.map(time -> Data.of(time, Long.valueOf(0))).collect(TreeSet::new, TreeSet::add, (left, right) -> {
					left.addAll(right);
				});

		Timeserie serieWithBlank = new Timeserie();
		serieWithBlank.addAll(results);
		serieWithBlank.addAll(serie);

		return serieWithBlank;
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
