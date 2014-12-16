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
