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
import io.mandrel.data.spider.SpiderService;
import io.mandrel.timeline.Event;
import io.mandrel.timeline.TimelineService;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/")
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HomeController {

	private final SpiderService spiderService;

	private final NodeService nodeService;

	private final TimelineService timelineService;

	@RequestMapping
	public String home(Model model) {
		model.addAttribute("spiders", spiderService.list().collect(Collectors.toList()));
		model.addAttribute("nodes", nodeService.nodes());
		List<Event> page = timelineService.page(0, 20);
		model.addAttribute("events", page.stream().collect(Collectors.groupingBy(event -> event.getTime().toLocalDate().toString())));
		return "views/home";
	}
}
