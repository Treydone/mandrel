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
package io.mandrel.metrics;

import io.mandrel.endpoints.contracts.coordinator.MetricsContract;
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
public class MetricsService implements MetricsContract {

	private final MetricsRepository metricsRepository;

	public void updateMetrics(Map<String, Long> accumulators) {
		metricsRepository.sync(accumulators);
	}

	public Timeserie getTimeserie(String name) {
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

	public NodeMetrics getNodeMetrics(String nodeId) {
		return metricsRepository.node(nodeId);
	}

	public GlobalMetrics getGlobalMetrics() {
		return metricsRepository.global();

	}

	public JobMetrics getJobMetrics(long jobId) {
		return metricsRepository.job(jobId);
	}

	public void deleteJobMetrics(long jobId) {
		metricsRepository.delete(jobId);
	}

	@Override
	public void close() throws Exception {

	}
}
