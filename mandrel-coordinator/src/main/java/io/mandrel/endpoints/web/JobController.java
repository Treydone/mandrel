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

import io.mandrel.common.data.Client;
import io.mandrel.common.data.Extractors;
import io.mandrel.common.data.Filters;
import io.mandrel.common.data.Job;
import io.mandrel.common.data.Politeness;
import io.mandrel.common.data.StoresDefinition;
import io.mandrel.data.source.Source;
import io.mandrel.data.source.Source.SourceDefinition;
import io.mandrel.frontier.Frontier.FrontierDefinition;
import io.mandrel.job.JobService;
import io.mandrel.metrics.MetricsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping(value = "/jobs")
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class JobController {

	private final JobService jobService;
	private final MetricsService metricsService;
	private final ObjectMapper mapper;

	@RequestMapping
	public String jobs(Model model, @PageableDefault(page = 0, size = 20) Pageable pageable) {
		model.addAttribute("jobs", jobService.page(pageable));
		return "views/jobs";
	}

	@RequestMapping("/{id}")
	public String job(@PathVariable long id, Model model) throws Exception {
		Job job = jobService.get(id);
		model.addAttribute("job", job);
		model.addAttribute("metrics", metricsService.job(id));

		refill(model, job);
		return "views/job";
	}

	@RequestMapping(value = "/{id}/start")
	public String start(@PathVariable Long id) {
		jobService.start(id);
		return "redirect:/jobs/{id}";
	}

	@RequestMapping(value = "/{id}/fork")
	public String fork(@PathVariable Long id, Model model) throws BindException {
		long newId = jobService.fork(id);
		model.addAttribute("newId", newId);
		return "redirect:/jobs/{newId}";
	}

	@RequestMapping(value = "/{id}/pause")
	public String pause(@PathVariable Long id) {
		jobService.pause(id);
		return "redirect:/jobs/{id}";
	}

	@RequestMapping(value = "/{id}/cancel")
	public String cancel(@PathVariable Long id) {
		jobService.kill(id);
		return "redirect:/jobs/{id}";
	}

	@RequestMapping(value = "/{id}/delete")
	public String delete(@PathVariable Long id) {
		jobService.delete(id);
		return "redirect:/jobs/{id}";
	}

	@RequestMapping(value = "/{id}/reinject")
	public String reinject(@PathVariable Long id) {
		jobService.reinject(id);
		return "redirect:/jobs/{id}";
	}

	@RequestMapping("/add")
	public String prepare(Model model) throws JsonProcessingException {
		return "views/job_add";
	}

	@RequestMapping("/add/definition")
	public String addWithDefinition(Model model) throws JsonProcessingException {
		prepareModel(model);
		return "views/job_add_with_def";
	}

	@RequestMapping(value = "/add/definition", method = RequestMethod.POST)
	public String createWithDefinition(Model model, @RequestParam String definition) throws JsonProcessingException {
		Job job;
		try {
			job = mapper.readValue(definition, Job.class);
		} catch (IOException e) {
			model.addAttribute("errors", "JSON invalid");
			log.debug("Job definition is invalid", e);
			return "redirect:/add/definition";
		}
		try {
			jobService.add(job);
		} catch (BindException e) {
			model.addAttribute("errors", e.getAllErrors());
			refill(model, job);
			log.debug("Can not add job", e);
			return "views/job_add_with_def";
		}
		return "redirect:/jobs";
	}

	@RequestMapping("/add/form")
	public String addWithForm(Model model) throws JsonProcessingException {
		prepareModel(model);
		return "views/job_add_with_form";
	}

	@RequestMapping(value = "/add/form", method = RequestMethod.POST)
	public String create(Model model, @RequestParam String definition) throws JsonProcessingException {
		Job job;
		try {
			job = mapper.readValue(definition, Job.class);
		} catch (IOException e) {
			model.addAttribute("errors", "JSON invalid");
			log.debug("Job definition is invalid", e);
			return "redirect:/add/form";
		}
		try {
			jobService.add(job);
		} catch (BindException e) {
			model.addAttribute("errors", e.getAllErrors());
			refill(model, job);
			log.debug("Can not add job", e);
			return "views/job_add_with_form";
		}
		return "redirect:/jobs";
	}

	private void prepareModel(Model model) throws JsonProcessingException {
		model.addAttribute("baseValue", mapper.writeValueAsString(new BaseValue()));
		model.addAttribute("storesValue", mapper.writeValueAsString(new StoresDefinition()));
		model.addAttribute("frontierValue", mapper.writeValueAsString(new FrontierDefinition()));
		model.addAttribute("extractionValue", mapper.writeValueAsString(new Extractors()));
		model.addAttribute("politenessValue", mapper.writeValueAsString(new Politeness()));
		model.addAttribute("advancedValue", mapper.writeValueAsString(new Client()));
	}

	private void refill(Model model, Job job) throws JsonProcessingException {
		model.addAttribute("json", mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(job));
		BaseValue value = new BaseValue();
		value.setName(job.getName());
		value.setSources(job.getSources());
		value.setFilters(job.getFilters());
		model.addAttribute("baseValue", mapper.writeValueAsString(value));
		model.addAttribute("storesValue", mapper.writeValueAsString(job.getStores()));
		model.addAttribute("frontierValue", mapper.writeValueAsString(job.getFrontier()));
		model.addAttribute("extractionValue", mapper.writeValueAsString(job.getExtractors()));
		model.addAttribute("politenessValue", mapper.writeValueAsString(job.getPoliteness()));
		model.addAttribute("advancedValue", mapper.writeValueAsString(job.getClient()));
	}

	@Data
	public static class BaseValue {

		@JsonProperty("name")
		private String name;

		@JsonProperty("sources")
		private List<SourceDefinition<? extends Source>> sources = new ArrayList<>();

		@JsonProperty("filters")
		private Filters filters = new Filters();

	}
}
