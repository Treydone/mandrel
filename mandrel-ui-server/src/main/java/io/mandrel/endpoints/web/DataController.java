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
import io.mandrel.common.data.Job;
import io.mandrel.common.data.PageRequest;
import io.mandrel.data.content.DataExtractor;
import io.mandrel.data.content.DefaultDataExtractor;
import io.mandrel.document.Document;
import io.mandrel.document.DocumentStore;
import io.mandrel.document.DocumentStores;
import io.mandrel.document.NavigableDocumentStore;
import io.mandrel.transport.MandrelClient;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DataController {

	//	private final JobService jobService;
	private final MandrelClient client;

	@RequestMapping("/data")
	public String data(Model model, @PageableDefault(page = 0, size = 20) Pageable pageable) {
		model.addAttribute("jobs",
				client.coordinator().jobs().onAny().map(s -> s.pageForActive(new PageRequest(pageable.getPageNumber(), pageable.getPageSize()))));
		return "views/data";
	}

	@RequestMapping("/jobs/{id}/data/{extractorName}")
	public String view(@PathVariable Long id, @PathVariable String extractorName, Model model) {
		Job job = client.coordinator().jobs().onAny().map(s -> s.get(id));
		model.addAttribute("job", job);

		DataExtractor extractor = job.getDefinition().getExtractors().getData().stream().filter(ex -> extractorName.equals(ex.getName())).findFirst()
				.orElseThrow(() -> new NotFoundException(""));

		DocumentStore theStore = DocumentStores.get(id, extractorName).orElseThrow(() -> new NotFoundException(""));
		if (!theStore.isNavigable()) {
			throw new NotImplementedException("Not a navigable document store");
		}

		model.addAttribute("extractor", extractor);
		return "views/data_job";
	}

	@RequestMapping(value = "/jobs/{id}/data/{extractor}", method = RequestMethod.POST)
	@ResponseBody
	public PageResponse data(@PathVariable Long id, @PathVariable String extractor, io.mandrel.endpoints.web.PageRequest request, Model model) {
		Job job = client.coordinator().jobs().onAny().map(s -> s.get(id));

		DocumentStore theStore = DocumentStores.get(id, extractor).orElseThrow(() -> new NotFoundException(""));

		if (!theStore.isNavigable()) {
			throw new NotImplementedException("Not a navigable document store");
		}
		NavigableDocumentStore store = (NavigableDocumentStore) theStore;

		DataExtractor theExtractor = job.getDefinition().getExtractors().getData().stream().filter(ex -> extractor.equals(ex.getName())).findFirst()
				.orElseThrow(() -> new NotFoundException(""));

		if (!(theExtractor instanceof DefaultDataExtractor)) {
			throw new NotImplementedException("Not a default data extractor");
		}

		Collection<Document> page = store.byPages(request.getLength(), request.getStart() / request.getLength());
		page.forEach(doc -> ((DefaultDataExtractor) theExtractor).getFields().forEach(f -> doc.putIfAbsent(f.getName(), Collections.emptyList())));
		Long total = store.total();

		PageResponse dataPage = PageResponse.of(page);
		dataPage.setDraw(request.getDraw());
		dataPage.setRecordsTotal(total);
		dataPage.setRecordsFiltered(total);
		return dataPage;
	}
}
