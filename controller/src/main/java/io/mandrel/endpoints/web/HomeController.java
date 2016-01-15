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
package io.mandrel.endpoints.web;

import io.mandrel.cluster.node.NodeService;
import io.mandrel.messaging.StompService;
import io.mandrel.metrics.MetricsRepository;
import io.mandrel.spider.SpiderService;
import io.mandrel.timeline.Event;
import io.mandrel.timeline.TimelineService;

import java.time.LocalDateTime;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(value = "/")
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HomeController {

	private final SpiderService spiderService;

	private final NodeService nodeService;

	private final TimelineService timelineService;

	private final StompService stompService;

	private final MetricsRepository metricsRepository;

	@RequestMapping
	public String home(Model model) {
		model.addAttribute("metrics", metricsRepository.global());
		model.addAttribute("spiders", spiderService.listLastActive(10));
		model.addAttribute("nodes", nodeService.nodes());
		model.addAttribute("events", timelineService.pageByDate(0, 10));
		return "views/home";
	}

	@RequestMapping("/publish")
	public String push(@RequestParam String title, @RequestParam String text) {
		Event event = new Event();
		event.setText(text);
		event.setTitle(title);
		event.setTime(LocalDateTime.now());
		stompService.publish(event);
		return "redirect:/";
	}
}
