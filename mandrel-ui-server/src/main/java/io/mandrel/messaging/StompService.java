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
package io.mandrel.messaging;

import io.mandrel.timeline.Event;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Template;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class StompService {

	private final SimpMessageSendingOperations messagingTemplate;
	private final freemarker.template.Configuration freeMarkerConfiguration;

	public void publish(Object message) {
		String name = null;
		Map<String, Object> headers = null;
		Map<String, Object> model = null;
		if (message instanceof Event) {
			name = "views/event.ftl";
			headers = Collections.singletonMap("type", "event");
			model = Collections.singletonMap("event", message);
		}

		if (name != null) {
			try {
				Template template = freeMarkerConfiguration.getTemplate(name);
				String body = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
				messagingTemplate.convertAndSend("/topic/global", body, headers);
			} catch (Exception e) {
				log.warn("Cannot send stomp notification", e);
			}
		}
	}
}
