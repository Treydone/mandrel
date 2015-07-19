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
package io.mandrel.monitor.console;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

@Component
public class ConsoleService {

	private final Environment env;

	private final SimpMessageSendingOperations simp;

	@Autowired
	public ConsoleService(Environment environment, SimpMessageSendingOperations simp) {
		this.env = environment;
		this.simp = simp;
	}

	@PostConstruct
	public void init() {
		Boolean isConsoleEnabled = env.getProperty("logging.console.enabled", Boolean.class, false);
		if (isConsoleEnabled) {
			String pattern = env.getRequiredProperty("logging.console.pattern", String.class);
			String level = env.getProperty("logging.console.level", String.class, "WARN");

			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

			ConsoleAppender appender = new ConsoleAppender();
			appender.setContext(loggerContext);
			appender.setPattern(pattern);
			appender.setMessagingTemplate(simp);
			appender.start();

			Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			logger.addAppender(appender);
			logger.setLevel(Level.valueOf(level));
			logger.setAdditive(false);
		}
	}
}
