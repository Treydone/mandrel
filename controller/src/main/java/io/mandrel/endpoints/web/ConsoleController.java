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

import io.mandrel.monitor.console.ConsoleService;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/console")
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ConsoleController {

	private final ConsoleService consoleService;

	@RequestMapping
	public String console(Model model) {
		return "views/console";
	}

	@RequestMapping("/deactivate")
	public String deactivate() {
		consoleService.deactivate();
		return "redirect:/console";
	}

	@RequestMapping("/activate")
	public String activate() {
		consoleService.activate();
		return "redirect:/console";
	}
}
