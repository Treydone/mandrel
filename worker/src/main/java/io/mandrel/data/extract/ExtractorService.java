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
package io.mandrel.data.extract;

import io.mandrel.blob.Blob;
import io.mandrel.common.data.Spider;
import io.mandrel.common.net.Uri;
import io.mandrel.data.Link;
import io.mandrel.data.content.DataExtractor;
import io.mandrel.data.content.DefaultDataExtractor;
import io.mandrel.data.content.OutlinkExtractor;
import io.mandrel.data.content.selector.DataConverter;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.document.Document;
import io.mandrel.document.DocumentStores;
import io.mandrel.metadata.MetadataStores;
import io.mandrel.script.ScriptingService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import us.codecraft.xsoup.xevaluator.XElement;

@Component
@Slf4j
public class ExtractorService {

	private final ScriptingService scriptingService;

	@Inject
	public ExtractorService(ScriptingService scriptingService) {
		super();
		this.scriptingService = scriptingService;
	}

	public Pair<Set<Link>, Set<Link>> extractAndFilterOutlinks(Spider spider, Uri uri, Map<String, Instance<?>> cachedSelectors, Blob blob, OutlinkExtractor ol) {
		// Find outlinks in page
		Set<Link> outlinks = extractOutlinks(cachedSelectors, blob, ol);
		log.trace("Finding outlinks for url {}: {}", uri, outlinks);

		// Filter outlinks
		Set<Link> filteredOutlinks = null;
		if (outlinks != null) {
			Stream<Link> stream = outlinks.stream().filter(l -> l != null && l.getUri() != null);
			if (spider.getFilters() != null && CollectionUtils.isNotEmpty(spider.getFilters().getLinks())) {
				stream = stream.filter(link -> spider.getFilters().getLinks().stream().allMatch(f -> f.isValid(link)));
			}
			filteredOutlinks = stream.collect(Collectors.toSet());
		}

		Set<Link> allFilteredOutlinks = null;
		if (filteredOutlinks != null) {
			Set<Uri> res = MetadataStores.get(spider.getId()).deduplicate(filteredOutlinks.stream().map(l -> l.getUri()).collect(Collectors.toList()));
			allFilteredOutlinks = filteredOutlinks.stream().filter(f -> res.contains(f.getUri())).collect(Collectors.toSet());
		}

		log.trace("And filtering {}", allFilteredOutlinks);
		return Pair.of(outlinks, allFilteredOutlinks);
	}

	public Set<Link> extractOutlinks(Map<String, Instance<?>> cachedSelectors, Blob blob, OutlinkExtractor extractor) {

		List<Link> outlinks = DefaultDataExtractor.extract(cachedSelectors, blob, null, extractor.getExtractor(), new DataConverter<XElement, Link>() {
			public Link convert(XElement element) {
				Link link = new Link();

				String uri = element.getElement().absUrl("href");
				link.setUri(StringUtils.isNotBlank(uri) ? Uri.create(uri) : null);

				String rel = element.getElement().attr("rel");
				link.setRel(StringUtils.isNotBlank(rel) ? rel : null);

				String title = element.getElement().attr("title");
				link.setTitle(StringUtils.isNotBlank(title) ? title : null);

				String text = element.getElement().ownText();
				link.setText(StringUtils.isNotBlank(text) ? text : null);
				return link;
			}
		});

		if (outlinks != null && !outlinks.isEmpty()) {
			outlinks = (List<Link>) DefaultDataExtractor.format(scriptingService, blob.getMetadata(), extractor, outlinks);
		}

		return new HashSet<>(outlinks);
	}

	public List<Document> extractThenFormatThenStore(long spiderId, Map<String, Instance<?>> cachedSelectors, Blob blob, DataExtractor extractor) {

		List<Document> documents = extractThenFormat(cachedSelectors, blob, extractor);

		// Store the result
		if (documents != null) {
			DocumentStores.get(spiderId, extractor.getName()).get().save(documents);
		}

		return documents;
	}

	public List<Document> extractThenFormat(Map<String, Instance<?>> cachedSelectors, Blob blob, DataExtractor extractor) {
		return extractor.extract(scriptingService, cachedSelectors, blob);
	}
}
