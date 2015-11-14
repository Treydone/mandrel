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

import io.mandrel.common.NotFoundException;
import io.mandrel.common.data.Spider;
import io.mandrel.controller.ControllerService;
import io.mandrel.metrics.MetricsRepository;

import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping(value = "/spiders")
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpiderController {

	private final ControllerService spiderService;

	private final MetricsRepository metricsRepository;

	private final ObjectMapper mapper;

	@RequestMapping
	public String spiders(Model model) {
		model.addAttribute("spiders", spiderService.list().collect(Collectors.toList()));
		return "views/spiders";
	}

	@RequestMapping("/{id}")
	public String spider(@PathVariable long id, Model model) throws Exception {
		Spider spider = spiderService.get(id).get();
		model.addAttribute("spider", spider);
		model.addAttribute("json", mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(spider));
		model.addAttribute("metrics", metricsRepository.spider(id));
		return "views/spider";
	}

	@RequestMapping(value = "/{id}/start")
	public String start(@PathVariable Long id) {
		spiderService.start(id).orElseThrow(() -> new NotFoundException("Spider not found"));
		return "redirect:/spiders/{id}";
	}

	@RequestMapping(value = "/{id}/pause")
	public String pause(@PathVariable Long id) {
		// TODO
		return "redirect:/spiders/{id}";
	}

	@RequestMapping(value = "/{id}/cancel")
	public String cancel(@PathVariable Long id) {
		spiderService.kill(id);
		return "redirect:/spiders/{id}";
	}

	@RequestMapping(value = "/{id}/delete")
	public String delete(@PathVariable Long id) {
		spiderService.delete(id);
		return "redirect:/spiders/{id}";
	}

	@RequestMapping("/add")
	public String prepare() {
		return "views/spider_add";
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String create(Model model, @RequestBody Spider spider) {
		try {
			spiderService.add(spider);
		} catch (BindException e) {
			return "views/spider_add";
		}
		return "redirect:/spiders";
	}
}
