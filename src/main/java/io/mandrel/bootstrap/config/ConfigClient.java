package io.mandrel.bootstrap.config;

import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;

import com.google.common.collect.Maps;

@Configuration
@EnableAutoConfiguration
public class ConfigClient {

	public static void main(String[] args) {
		Map<String, Object> properties = Maps.newHashMap();
		properties.put("spring.config.location", "classpath:/version.yml");

		ConfigurableApplicationContext context = new SpringApplicationBuilder(ConfigClient.class).properties(properties).run(args);

		MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
		propertySources.forEach(ps -> {
			System.err.println("-------------" + ps.getName());
			if (ps instanceof EnumerablePropertySource) {
				String[] propertyNames = ((EnumerablePropertySource<?>) ps).getPropertyNames();
				IntStream.range(0, propertyNames.length).forEach(i -> System.err.println(propertyNames[i] + ": " + ps.getProperty(propertyNames[i])));
			}
		});
	}
}
