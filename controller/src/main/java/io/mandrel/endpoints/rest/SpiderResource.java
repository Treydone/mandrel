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
import io.mandrel.metrics.MetricsService;
import io.mandrel.metrics.SpiderMetrics;
import io.mandrel.spider.SpiderService;
import io.mandrel.transport.Clients;

import java.util.List;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

@Api(value = "/spiders")
@RequestMapping(value = Apis.PREFIX + "/spiders", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpiderResource {

	private final Clients clients;
	private final SpiderService spiderService;
	private final MetricsService metricsService;

	@ApiOperation(value = "List all the spiders", httpMethod = "GET", response = Spider.class, responseContainer = "List")
	@RequestMapping(method = RequestMethod.GET)
	public Page<Spider> all(@PageableDefault(page = 0, size = 20) Pageable pageable) {
		return spiderService.page(pageable);
	}

	@ApiOperation(value = "Add a spider", httpMethod = "GET")
	@RequestMapping(method = RequestMethod.GET, params = "urls")
	public Spider add(@RequestParam List<String> urls) throws BindException {
		return spiderService.add(urls);
	}

	@ApiOperation(value = "Add a spider", httpMethod = "POST")
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Spider add(@RequestBody Spider spider) throws BindException {
		return spiderService.add(spider);
	}

	@ApiOperation(value = "Update a spider", httpMethod = "PUT", response = Spider.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Spider update(@PathVariable Long id, @RequestBody Spider spider) throws BindException {
		return spiderService.update(spider);
	}

	@ApiOperation(value = "Find a spider by its id", httpMethod = "GET", response = Spider.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Spider id(@PathVariable Long id) {
		return spiderService.get(id);
	}

	@ApiOperation(value = "Start a spider", httpMethod = "GET")
	@RequestMapping(value = "/{id}/start", method = RequestMethod.GET)
	public void start(@PathVariable Long id) {
		spiderService.start(id);
	}

	@ApiOperation(value = "Analyze a source against a spider", httpMethod = "GET")
	@RequestMapping(value = "/{id}/analyze", method = RequestMethod.GET)
	public byte[] analyze(@PathVariable Long id, @RequestParam String source) {
		return clients.onRandomWorker().map(w -> w.analyse(id, source));
	}

	@ApiOperation(value = "Pause a spider", httpMethod = "GET")
	@RequestMapping(value = "/{id}/pause", method = RequestMethod.GET)
	public void pause(@PathVariable Long id) {
		spiderService.pause(id);
	}

	@ApiOperation(value = "Cancel a spider", httpMethod = "GET")
	@RequestMapping(value = "/{id}/cancel", method = RequestMethod.GET)
	public void cancel(@PathVariable Long id) {
		spiderService.kill(id);
	}

	@ApiOperation(value = "Delete a spider", httpMethod = "DELETE")
	@RequestMapping(value = "/{id}/delete", method = RequestMethod.DELETE)
	public void delete(@PathVariable Long id) {
		spiderService.delete(id);
	}

	@ApiOperation(value = "Retrieve the stats of a spider", httpMethod = "GET")
	@RequestMapping(value = "/{id}/stats", method = RequestMethod.GET)
	public SpiderMetrics stats(@PathVariable Long id) {
		Spider spider = spiderService.get(id);
		return metricsService.spider(spider.getId());
	}
}
