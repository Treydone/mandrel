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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.LayoutBase;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ConsoleService {

	private final Environment env;

	private final SimpMessageSendingOperations simp;

	private ConsoleAppender appender;

	@PostConstruct
	public void init() {
		Boolean isConsoleEnabled = env.getProperty("logging.console.enabled", Boolean.class, false);
		if (isConsoleEnabled) {
			String level = env.getProperty("logging.console.level", String.class, "WARN");

			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

			Layout<ILoggingEvent> layout = new LayoutBase<ILoggingEvent>() {

				@Override
				public String doLayout(ILoggingEvent event) {
					StringBuilder builder = new StringBuilder();
					builder.append("<tr>");
					append(builder, DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.ofEpochSecond(event.getTimeStamp() / 1000, 0, ZoneOffset.UTC)));

					builder.append("<td><span class=\"label label-");
					switch (event.getLevel().toString()) {
					case "ERROR":
						builder.append("danger");
						break;
					case "WARN":
						builder.append("warning");
						break;
					case "INFO":
						builder.append("info");
						break;
					case "DEBUG":
						builder.append("primary");
						break;
					case "TRACE":
						builder.append("success");
						break;
					}
					builder.append("\">").append(event.getLevel().toString()).append("</span></td>");

					append(builder, event.getThreadName());
					append(builder, event.getLoggerName());
					append(builder, event.getMessage());
					builder.append("</tr>");
					return builder.toString();
				}

				public void append(StringBuilder builder, Object data) {
					builder.append("<td>").append(data).append("</td>");
				}
			};

			layout.setContext(loggerContext);
			layout.start();

			ConsoleAppender appender = new ConsoleAppender();
			appender.setContext(loggerContext);
			appender.setLayout(layout);
			appender.setMessagingTemplate(simp);
			appender.start();

			Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			logger.addAppender(appender);
			logger.setLevel(Level.valueOf(level));
			logger.setAdditive(false);
		}
	}

	public void deactivate() {
		appender.setActivated(false);
	}

	public void activate() {
		appender.setActivated(true);
	}

	public boolean isActivate() {
		return appender.isActivated();
	}
}
