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
package io.mandrel.config;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;

@Configuration
public class MetricConfiguration {

	@Configuration
	@ConditionalOnProperty("graphite.enabled")
	static class Default {

		@Inject
		private MetricRegistry metricRegistry;

		@Bean
		public GraphiteReporter graphiteReporter(Environment environment) {
			final Graphite graphite = new Graphite(new InetSocketAddress(
					environment.getProperty("graphite.host"),
					environment.getRequiredProperty("graphite.port",
							Integer.class)));
			final GraphiteReporter reporter = GraphiteReporter
					.forRegistry(metricRegistry).prefixedWith("webapp")
					.convertRatesTo(TimeUnit.SECONDS)
					.convertDurationsTo(TimeUnit.MILLISECONDS)
					.filter(MetricFilter.ALL).build(graphite);
			reporter.start(10, TimeUnit.SECONDS);
			return reporter;
		}
	}
}
