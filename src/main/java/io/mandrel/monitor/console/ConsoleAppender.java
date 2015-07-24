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

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.messaging.simp.SimpMessageSendingOperations;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConsoleAppender extends AppenderBase<ILoggingEvent> {

	private SimpMessageSendingOperations messagingTemplate;

	private Layout<ILoggingEvent> layout;

	private AtomicBoolean activated = new AtomicBoolean(true);

	@Override
	protected void append(ILoggingEvent event) {
		if (!isStarted()) {
			return;
		}

		if (isActivated()) {
			messagingTemplate.convertAndSend("/topic/tail", layout.doLayout(event));
		}
	}

	public boolean isActivated() {
		return activated.get();
	}

	public void setActivated(boolean activated) {
		this.activated.set(activated);
	}
}
