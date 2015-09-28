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
import io.mandrel.data.content.MetadataExtractor;
import io.mandrel.data.spider.SpiderService;
import io.mandrel.endpoints.PageRequest;
import io.mandrel.endpoints.PageResponse;
import io.mandrel.gateway.Document;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DataController {

	private final SpiderService spiderService;

	@RequestMapping("/data")
	public String data(Model model) {
		model.addAttribute("spiders", spiderService.list().collect(Collectors.toList()));
		return "views/data";
	}

	@RequestMapping("/spiders/{id}/data/{extractor}")
	public String view(@PathVariable Long id, @PathVariable String extractor, Model model) {
		Spider spider = spiderService.get(id).get();
		model.addAttribute("spider", spider);
		model.addAttribute("extractor",
				spider.getExtractors().getPages().stream().filter(ex -> extractor.equals(ex.getName())).findFirst()
						.orElseThrow(() -> new NotFoundException("")));
		return "views/data_spider";
	}

	@RequestMapping(value = "/spiders/{id}/data/{extractor}", method = RequestMethod.POST)
	@ResponseBody
	public PageResponse data(@PathVariable Long id, @PathVariable String extractor, PageRequest request, Model model) {
		Spider spider = spiderService.get(id).get();

		spiderService.injectAndInit(spider);

		MetadataExtractor theExtractor = spider.getExtractors().getPages().stream().filter(ex -> extractor.equals(ex.getName())).findFirst()
				.orElseThrow(() -> new NotFoundException(""));

		theExtractor.getDocumentStore().init(theExtractor);

		Collection<Document> page = theExtractor.getDocumentStore().byPages(spider.getId(), request.getLength(), request.getStart() / request.getLength());
		page.forEach(doc -> theExtractor.getFields().forEach(f -> doc.putIfAbsent(f.getName(), Collections.emptyList())));
		Long total = theExtractor.getDocumentStore().total(spider.getId());

		PageResponse dataPage = PageResponse.of(page);
		dataPage.setDraw(request.getDraw());
		dataPage.setRecordsTotal(total);
		dataPage.setRecordsFiltered(total);
		return dataPage;
	}
}
