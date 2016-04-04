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

import io.mandrel.metrics.MetricKeys;
import io.mandrel.metrics.MetricsService;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MetricsController {

	private final MetricsService metricsService;

	@RequestMapping("/metrics")
	public String data(Model model, @PageableDefault(page = 0, size = 20) Pageable pageable) {
		model.addAttribute("metrics", metricsService.global());
		model.addAttribute("totalSize", metricsService.serie(MetricKeys.globalTotalSize()));
		model.addAttribute("nbPages", metricsService.serie(MetricKeys.globalNbPages()));
		return "views/metrics";
	}
}
