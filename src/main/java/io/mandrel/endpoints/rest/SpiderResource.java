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
package io.mandrel.endpoints.rest;

import io.mandrel.common.data.Spider;
import io.mandrel.data.analysis.Analysis;
import io.mandrel.data.analysis.AnalysisService;
import io.mandrel.data.spider.SpiderService;
import io.mandrel.metrics.MetricsService;
import io.mandrel.metrics.SpiderMetrics;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/spiders")
@RequestMapping(value = Apis.PREFIX + "/spiders", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpiderResource {

	private final AnalysisService analysisService;

	private final SpiderService spiderService;

	private final MetricsService statsService;

	@ApiOperation(value = "List all the spiders", response = Spider.class, responseContainer = "List")
	@RequestMapping(method = RequestMethod.GET)
	public List<Spider> all() {
		return spiderService.list().collect(Collectors.toList());
	}

	@ApiOperation(value = "Add a spider")
	@RequestMapping(method = RequestMethod.GET, params = "urls")
	public Spider add(@RequestParam List<String> urls) throws BindException {
		return spiderService.add(urls);
	}

	@ApiOperation(value = "Add a spider")
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Spider add(@RequestBody Spider spider) throws BindException {
		return spiderService.add(spider);
	}

	@ApiOperation(value = "Update a spider", response = Spider.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Spider update(@PathVariable Long id, @RequestBody Spider spider) throws BindException {
		return spiderService.update(spider);
	}

	@ApiOperation(value = "Find a spider by its id", response = Spider.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Optional<Spider> id(@PathVariable Long id) {
		return spiderService.get(id).map(opt -> opt);
	}

	@ApiOperation(value = "Start a spider")
	@RequestMapping(value = "/{id}/start", method = RequestMethod.GET)
	public Optional<Spider> start(@PathVariable Long id) {
		return spiderService.start(id);
	}

	@ApiOperation(value = "Analyze a source against a spider")
	@RequestMapping(value = "/{id}/analyze", method = RequestMethod.GET)
	public Optional<Analysis> analyze(@PathVariable Long id, @RequestParam String source) {
		return spiderService.get(id).map(spider -> analysisService.analyze(spider, source));
	}

	@ApiOperation(value = "Pause a spider")
	@RequestMapping(value = "/{id}/pause", method = RequestMethod.GET)
	public void pause(@PathVariable Long id) {
		// TODO
	}

	@ApiOperation(value = "Cancel a spider")
	@RequestMapping(value = "/{id}/cancel", method = RequestMethod.GET)
	public Optional<Spider> cancel(@PathVariable Long id) {
		return spiderService.cancel(id);
	}

	@ApiOperation(value = "Delete a spider")
	@RequestMapping(value = "/{id}/delete", method = RequestMethod.DELETE)
	public Optional<Spider> delete(@PathVariable Long id) {
		return spiderService.delete(id);
	}

	@ApiOperation(value = "Retrieve the stats of a spider")
	@RequestMapping(value = "/{id}/stats", method = RequestMethod.GET)
	public Optional<SpiderMetrics> stats(@PathVariable Long id) {
		return spiderService.get(id).map(spider -> statsService.spider(spider.getId()));
	}
}
