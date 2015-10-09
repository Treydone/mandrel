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

import io.mandrel.common.data.Spider;
import io.mandrel.common.loader.NamedProviders;
import io.mandrel.data.content.Extractor;
import io.mandrel.data.content.FieldExtractor;
import io.mandrel.data.content.Formatter;
import io.mandrel.data.content.MetadataExtractor;
import io.mandrel.data.content.NamedDataExtractorFormatter;
import io.mandrel.data.content.OutlinkExtractor;
import io.mandrel.data.content.SourceType;
import io.mandrel.data.content.selector.BodySelector;
import io.mandrel.data.content.selector.CookieSelector;
import io.mandrel.data.content.selector.DataConverter;
import io.mandrel.data.content.selector.EmptySelector;
import io.mandrel.data.content.selector.HeaderSelector;
import io.mandrel.data.content.selector.Selector;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.data.content.selector.UrlSelector;
import io.mandrel.data.spider.Link;
import io.mandrel.document.Document;
import io.mandrel.metadata.FetchMetadata;
import io.mandrel.requests.http.Cookie;
import io.mandrel.script.ScriptingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import us.codecraft.xsoup.xevaluator.XElement;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

@Component
@Slf4j
public class ExtractorService {

	private final ScriptingService scriptingService;

	@Inject
	public ExtractorService(ScriptingService scriptingService) {
		super();
		this.scriptingService = scriptingService;
	}

	public Pair<Set<Link>, Set<String>> extractAndFilterOutlinks(Spider spider, String url, Map<String, Instance<?>> cachedSelectors, FetchMetadata data,
			OutlinkExtractor ol) {
		// Find outlinks in page
		Set<Link> outlinks = extractOutlinks(cachedSelectors, data, ol);
		log.trace("Finding outlinks for url {}: {}", url, outlinks);

		// Filter outlinks
		Set<Link> filteredOutlinks = null;
		if (outlinks != null) {
			Stream<Link> stream = outlinks.stream().filter(l -> l != null && StringUtils.isNotBlank(l.getUri()));
			if (spider.getFilters() != null && CollectionUtils.isNotEmpty(spider.getFilters().getForLinks())) {
				stream = stream.filter(link -> spider.getFilters().getForLinks().stream().allMatch(f -> f.isValid(link)));
			}
			filteredOutlinks = stream.collect(Collectors.toSet());
		}

		Set<String> allFilteredOutlinks = null;
		if (filteredOutlinks != null) {
			allFilteredOutlinks = spider.getStores().getMetadataStore().filter(spider.getId(), filteredOutlinks, spider.getClient().getPoliteness());
		}
		log.trace("And filtering {}", filteredOutlinks);
		return Pair.of(outlinks, allFilteredOutlinks);
	}

	public Set<Link> extractOutlinks(Map<String, Instance<?>> cachedSelectors, FetchMetadata data, OutlinkExtractor extractor) {

		List<Link> outlinks = extract(cachedSelectors, data, null, extractor.getExtractor(), new DataConverter<XElement, Link>() {
			public Link convert(XElement element) {
				Link link = new Link();

				String uri = element.getElement().absUrl("href");
				link.setUri(StringUtils.isNotBlank(uri) ? uri : null);

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
			outlinks = (List<Link>) format(data, extractor, outlinks);
		}

		return new HashSet<>(outlinks);
	}

	public List<Document> extractThenFormatThenStore(long spiderId, Map<String, Instance<?>> cachedSelectors, FetchMetadata data, MetadataExtractor extractor) {

		List<Document> documents = extractThenFormat(cachedSelectors, data, extractor);

		// Store the result
		if (documents != null) {
			extractor.getDocumentStore().save(spiderId, documents);
		}

		return documents;
	}

	public List<Document> extractThenFormat(Map<String, Instance<?>> cachedSelectors, FetchMetadata data, MetadataExtractor extractor) {
		Preconditions.checkNotNull(extractor.getFields(), "No field for this extractor...");
		Preconditions.checkNotNull(extractor.getDocumentStore(), "No datastore for this extractor...");
		Preconditions.checkNotNull(cachedSelectors, "Cached selectors can not be null...");

		List<Document> documents = null;

		if (extractor.getFilters() == null && extractor.getFilters().getForLinks() == null || extractor.getFilters().getForLinks() != null
				&& extractor.getFilters().getForLinks().stream().allMatch(f -> f.isValid(new Link().setUri(data.getUri())))) {

			if (extractor.getMultiple() != null) {

				documents = new ArrayList<>();

				// Extract the multiple
				List<String> segments = extract(cachedSelectors, data, null, extractor.getMultiple(), DataConverter.BODY);

				documents = segments.stream().map(segment -> {

					Document document = new Document(extractor.getFields().size());

					for (FieldExtractor field : extractor.getFields()) {

						// Extract the value
						List<? extends Object> results = null;

						boolean isBody = SourceType.BODY.equals(field.getExtractor().getSource());
						if (field.isUseMultiple() && isBody) {
							results = extract(cachedSelectors, data, segment.getBytes(Charsets.UTF_8), field.getExtractor(), DataConverter.BODY);
						} else {
							DataConverter<?, String> converter = isBody ? DataConverter.BODY : DataConverter.DEFAULT;
							results = extract(cachedSelectors, data, null, field.getExtractor(), converter);
						}

						fillDocument(data, document, field, results);
					}

					return document;
				}).collect(Collectors.toList());

			} else {

				Document document = new Document(extractor.getFields().size());

				for (FieldExtractor field : extractor.getFields()) {

					// Extract the value
					boolean isBody = SourceType.BODY.equals(field.getExtractor().getSource());
					DataConverter<?, String> converter = isBody ? DataConverter.BODY : DataConverter.DEFAULT;
					List<? extends Object> results = extract(cachedSelectors, data, null, field.getExtractor(), converter);

					fillDocument(data, document, field, results);
				}

				documents = Arrays.asList(document);

			}
		}
		return documents;
	}

	public void fillDocument(FetchMetadata data, Document document, FieldExtractor field, List<? extends Object> results) {
		if (results != null && !results.isEmpty()) {

			if (field.isFirstOnly()) {
				results = results.subList(0, 1);
			}

			results = format(data, field, results);

			// Add it
			document.put(field.getName(), results);
		}
	}

	public <T, U> List<U> extract(Map<String, Instance<?>> selectors, FetchMetadata data, byte[] segment, Extractor fieldExtractor,
			DataConverter<T, U> converter) {
		Preconditions.checkNotNull(fieldExtractor, "There is no field extractor...");
		Preconditions.checkNotNull(fieldExtractor.getType(), "Extractor without type");
		// Preconditions.checkNotNull(fieldExtractor.getValue(),
		// "Extractor without value");

		Instance<T> instance;
		if (segment != null) {
			Selector<T> selector = getSelector(fieldExtractor);
			instance = ((BodySelector<T>) selector).init(data, segment, true);
		} else {
			// Reuse the previous instance selector for this web page
			String cacheKey = fieldExtractor.getType() + "-" + fieldExtractor.getSource().toString().toLowerCase(Locale.ROOT);
			instance = (Instance<T>) selectors.get(cacheKey);

			if (instance == null) {
				Selector<T> selector = getSelector(fieldExtractor);

				if (SourceType.BODY.equals(fieldExtractor.getSource())) {
					instance = ((BodySelector<T>) selector).init(data, data.getBody(), false);
				} else if (SourceType.HEADERS.equals(fieldExtractor.getSource())) {
					instance = ((HeaderSelector<T>) selector).init(data, data.getMetadata().getHeaders());
				} else if (SourceType.URL.equals(fieldExtractor.getSource())) {
					instance = ((UrlSelector<T>) selector).init(data, data.getUri());
				} else if (SourceType.COOKIE.equals(fieldExtractor.getSource())) {
					instance = ((CookieSelector<T>) selector).init(
							data,
							data.getMetadata()
									.getCookies()
									.stream()
									.map(cookie -> new Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getExpires(),
											cookie.getMaxAge(), cookie.isSecure(), cookie.isHttpOnly())).collect(Collectors.toList()));
				} else if (SourceType.EMPTY.equals(fieldExtractor.getSource())) {
					instance = ((EmptySelector<T>) selector).init(data);
				}

				selectors.put(cacheKey, instance);
			}
		}

		return instance.select(fieldExtractor.getValue(), converter);
	}

	private <T> Selector<T> getSelector(Extractor fieldExtractor) {
		Selector selector = NamedProviders.get(Selector.class, fieldExtractor.getType());
		if (selector == null) {
			throw new IllegalArgumentException("Unknown extractor '" + fieldExtractor.getType() + "'");
		}
		return selector;
	}

	public List<? extends Object> format(FetchMetadata data, NamedDataExtractorFormatter dataExtractorFormatter, List<? extends Object> results) {
		// ... and format it if necessary
		Formatter formatter = dataExtractorFormatter.getFormatter();
		if (formatter != null) {
			// TODO: optimize by reusing the previous engine if it is
			// the same scripting language
			ScriptEngine engine = scriptingService.getEngineByName(formatter.getType());

			return results.stream().map(result -> {
				ScriptContext bindings = scriptingService.getBindings(data, result);
				try {
					return scriptingService.execScript(new String(formatter.getValue(), Charsets.UTF_8), engine, bindings);
				} catch (Exception e) {
					log.debug("Can not format '{}': {}", dataExtractorFormatter.getName(), e);
				}
				return null;
			}).collect(Collectors.toList());

		}
		return results;
	}

}
