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

import java.time.LocalDateTime;

import io.mandrel.common.MandrelIllegalArgumentException;
import io.mandrel.common.NotFoundException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ErrorsController {

	private final ErrorAttributes errorAttributes;

	@RequestMapping("/error")
	public String error(Model model, HttpServletRequest request) {
		RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		model.addAttribute("time", LocalDateTime.now());
		model.addAllAttributes(errorAttributes.getErrorAttributes(requestAttributes, true));
		return "views/error";
	}

	@RequestMapping("/500/test")
	public String exemple500() {
		throw new MandrelIllegalArgumentException("This exception have to be shown only for admin!");
	}

	@RequestMapping("/404/test")
	public String exemple404() {
		throw new NotFoundException("This exception have to be shown only for admin!");
	}
}
