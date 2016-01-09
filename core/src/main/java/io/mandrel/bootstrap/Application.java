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
import io.mandrel.monitor.SigarService;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public abstract class Application {

	private ConfigurableApplicationContext context;

	public abstract void configure(SpringApplicationBuilder builder);

	public void start(String[] args) {

		Map<String, Object> properties = new HashMap<>();
		// properties.put("debug", "true");

		// Print some useful infos about the classpath and others things
		// new JHades().overlappingJarsReport();

		SpringApplicationBuilder builder = new SpringApplicationBuilder(getClass()).properties(properties).listeners(new ApplicationPidFileWriter());
		configure(builder);
		context = builder.run(args);
		context.start();

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
