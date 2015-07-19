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
package io.mandrel.bootstrap;

import io.mandrel.common.settings.InfoSettings;
import io.mandrel.endpoints.rest.ApiOriginFilter;
import io.mandrel.monitor.SigarService;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.jhades.JHades;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.actuate.system.EmbeddedServerPortFileWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("io.mandrel")
@Slf4j
public class Application extends SpringBootServletInitializer {

	private ConfigurableApplicationContext context;

	@Bean
	public FilterRegistrationBean originFilter() {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		filterRegistrationBean.setFilter(new ApiOriginFilter());
		filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));
		return filterRegistrationBean;
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {
		new Application().start(args);
	}

	public void start(String[] args) {

		// Print some useful infos about the classpath and others things
		new JHades().overlappingJarsReport();

		context = SpringApplication.run(Application.class, args);
		context.addApplicationListener(new ApplicationPidFileWriter());
		context.addApplicationListener(new EmbeddedServerPortFileWriter());

		InfoSettings settings = context.getBean(InfoSettings.class);
		log.info("{} ({}) started", settings.getArtifact(), settings.getVersion());

		SigarService sigar = context.getBean(SigarService.class);
		if (sigar.infos().getLimits().getOpenfiles().getMax() < 10000) {
			log.warn("Max openfiles limit is too low (open: {}, max: {})", sigar.infos().getLimits().getOpenfiles().getCurrent(), sigar.infos().getLimits()
					.getOpenfiles().getMax());
		}

	}

	public void stop() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500L);
				} catch (InterruptedException ex) {
					// Swallow exception and continue
				}
				context.close();
			}
		}).start();
	}
}
