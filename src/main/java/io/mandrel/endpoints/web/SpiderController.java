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

import io.mandrel.data.spider.SpiderService;

import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/spiders")
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpiderController {

	private final SpiderService spiderService;

	@RequestMapping
	public String spiders(Model model) {
		model.addAttribute("spiders", spiderService.list().collect(Collectors.toList()));
		return "views/spiders";
	}

	@RequestMapping("/{id}")
	public String spider(@PathVariable long id, Model model) {
		model.addAttribute("spiders", spiderService.get(id));
		return "views/spider";
	}
}
