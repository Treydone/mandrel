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
package io.mandrel.endpoints.contracts;

import io.mandrel.common.data.Spider;
import io.mandrel.endpoints.rest.Apis;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = Apis.PREFIX + "/workers", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public interface WorkerContract {

	@RequestMapping(value = "/sync", method = RequestMethod.POST)
	public void sync(@RequestBody List<Spider> spiders, @RequestHeader("target") URI target);

	@RequestMapping(value = "/active", method = RequestMethod.GET)
	public Map<Long, Long> listActive(@RequestHeader("target") URI target);

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public void create(@RequestBody Spider spider, @RequestHeader("target") URI target);

	@RequestMapping(value = "/{id}/start", method = RequestMethod.GET)
	public void start(@PathVariable("id") Long id, @RequestHeader("target") URI target);

	@RequestMapping(value = "/{id}/pause", method = RequestMethod.GET)
	public void pause(@PathVariable("id") Long id, @RequestHeader("target") URI target);

	@RequestMapping(value = "/{id}/kill", method = RequestMethod.GET)
	public void kill(@PathVariable("id") Long id, @RequestHeader("target") URI target);
}
