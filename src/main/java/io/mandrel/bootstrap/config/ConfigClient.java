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
package io.mandrel.bootstrap.config;

import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;

import com.google.common.collect.Maps;

@Configuration
@EnableAutoConfiguration
@EnableEurekaClient
public class ConfigClient {

	public static void main(String[] args) {
		Map<String, Object> properties = Maps.newHashMap();
		properties.put("spring.config.location", "classpath:/version.yml");

		ConfigurableApplicationContext context = new SpringApplicationBuilder(ConfigClient.class).web(false).properties(properties).run(args);

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
