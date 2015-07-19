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
package io.mandrel.cluster.yarn;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.stereotype.Component;
import org.springframework.yarn.boot.condition.ConditionalOnYarnContainer;

@EnableAutoConfiguration
public class ContainerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContainerApplication.class, args);
	}

	@Component
	@Slf4j
	public static class HelloPojo {

		@Autowired
		private Configuration configuration;

		@ConditionalOnYarnContainer
		public void publicVoidNoArgsMethod() throws IOException {
			log.info("Hello from HelloPojo");
			log.info("About to list from hdfs root content");
			try (FsShell shell = new FsShell(configuration)) {
				for (FileStatus s : shell.ls(false, "/")) {
					log.info(s.toString());
				}
			}
		}
	}
}
