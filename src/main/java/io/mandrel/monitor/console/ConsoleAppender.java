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

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.messaging.simp.SimpMessageSendingOperations;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConsoleAppender extends AppenderBase<ILoggingEvent> {

	private SimpMessageSendingOperations messagingTemplate;

	private PatternLayout layout;
	private String pattern;
	private boolean outputPatternAsHeader;

	@Override
	public void start() {
		PatternLayout patternLayout = new PatternLayout();
		patternLayout.setContext(context);
		patternLayout.setPattern(getPattern());
		patternLayout.setOutputPatternAsHeader(outputPatternAsHeader);
		patternLayout.start();
		this.layout = patternLayout;
		super.start();
	}

	@Override
	protected void append(ILoggingEvent event) {
		if (!isStarted()) {
			return;
		}

		messagingTemplate.convertAndSend("/topic/tail", layout.doLayout(event));
	}
}
