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
//package io.mandrel.bootstrap.config;
//
//import java.util.Map;
//
//import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
//import org.springframework.boot.actuate.system.EmbeddedServerPortFileWriter;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.builder.SpringApplicationBuilder;
//import org.springframework.cloud.config.server.EnableConfigServer;
//import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
//import org.springframework.context.annotation.Configuration;
//
//import com.google.common.collect.Maps;
//
//@Configuration
//@EnableAutoConfiguration
//@EnableEurekaServer
//@EnableConfigServer
//public class ConfigServer {
//
//	public static void main(String[] args) {
//		Map<String, Object> properties = Maps.newHashMap();
//		// properties.put("debug", "true");
//		properties.put("spring.profiles.active", "native");
//		properties.put("spring.config.location", "classpath:/version.yml,classpath:/configsrv.yml");
//
//		new SpringApplicationBuilder(ConfigServer.class).properties(properties).listeners(new ApplicationPidFileWriter(), new EmbeddedServerPortFileWriter())
//				.run(args);
//	}
//}
