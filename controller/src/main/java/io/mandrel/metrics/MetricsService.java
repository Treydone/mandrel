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

		LocalDateTime firstTime = serie.first().getTime();

		Set<Data> results = LongStream.range(0, Duration.between(firstTime, serie.last().getTime()).toMinutes())
				.mapToObj(minutes -> firstTime.plusMinutes(minutes)).map(time -> Data.of(time, Long.valueOf(0)))
				.collect(TreeSet::new, TreeSet::add, (left, right) -> {
					left.addAll(right);
				});

		Timeserie serieWithBlank = new Timeserie();
		serieWithBlank.addAll(results);
		serieWithBlank.addAll(serie);

		return serie;
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
