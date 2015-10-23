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
import io.mandrel.blob.BlobMetadata;
import io.mandrel.common.data.Spider;
import io.mandrel.common.loader.NamedProviders;
import io.mandrel.data.Link;
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
import io.mandrel.document.Document;
import io.mandrel.document.DocumentStores;
import io.mandrel.io.Payloads;
import io.mandrel.script.ScriptingService;

import java.net.URI;
import java.net.URISyntaxException;
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
import com.netflix.servo.util.Throwables;

@Component
@Slf4j
public class ExtractorService {

	private final ScriptingService scriptingService;

	@Inject
	public ExtractorService(ScriptingService scriptingService) {
		super();
		this.scriptingService = scriptingService;
	}

	public Pair<Set<Link>, Set<Link>> extractAndFilterOutlinks(Spider spider, URI uri, Map<String, Instance<?>> cachedSelectors, Blob blob, OutlinkExtractor ol) {
		// Find outlinks in page
		Set<Link> outlinks = extractOutlinks(cachedSelectors, blob, ol);
		log.trace("Finding outlinks for url {}: {}", uri, outlinks);

		// Filter outlinks
		Set<Link> filteredOutlinks = null;
		if (outlinks != null) {
			Stream<Link> stream = outlinks.stream().filter(l -> l != null && l.uri() != null);
			if (spider.getFilters() != null && CollectionUtils.isNotEmpty(spider.getFilters().getForLinks())) {
				stream = stream.filter(link -> spider.getFilters().getForLinks().stream().allMatch(f -> f.isValid(link)));
			}
			filteredOutlinks = stream.collect(Collectors.toSet());
		}

		log.trace("And filtering {}", filteredOutlinks);
		return Pair.of(outlinks, filteredOutlinks);
	}

	public Set<Link> extractOutlinks(Map<String, Instance<?>> cachedSelectors, Blob blob, OutlinkExtractor extractor) {

		List<Link> outlinks = extract(cachedSelectors, blob, null, extractor.getExtractor(), new DataConverter<XElement, Link>() {
			public Link convert(XElement element) {
				Link link = new Link();

				String uri = element.getElement().absUrl("href");
				try {
					link.uri(StringUtils.isNotBlank(uri) ? new URI(uri) : null);
				} catch (URISyntaxException e) {
					throw Throwables.propagate(e);
				}

				String rel = element.getElement().attr("rel");
				link.rel(StringUtils.isNotBlank(rel) ? rel : null);

				String title = element.getElement().attr("title");
				link.title(StringUtils.isNotBlank(title) ? title : null);

				String text = element.getElement().ownText();
				link.text(StringUtils.isNotBlank(text) ? text : null);
				return link;
			}
		});

		if (outlinks != null && !outlinks.isEmpty()) {
			outlinks = (List<Link>) format(blob.metadata(), extractor, outlinks);
		}

		return new HashSet<>(outlinks);
	}

	public List<Document> extractThenFormatThenStore(long spiderId, Map<String, Instance<?>> cachedSelectors, Blob blob, MetadataExtractor extractor) {

		List<Document> documents = extractThenFormat(cachedSelectors, blob, extractor);

		// Store the result
		if (documents != null) {
			DocumentStores.get(spiderId, extractor.getName()).get().save(documents);
		}

		return documents;
	}

	public List<Document> extractThenFormat(Map<String, Instance<?>> cachedSelectors, Blob blob, MetadataExtractor extractor) {
		Preconditions.checkNotNull(extractor.getFields(), "No field for this extractor...");
		Preconditions.checkNotNull(extractor.getDocumentStore(), "No datastore for this extractor...");
		Preconditions.checkNotNull(cachedSelectors, "Cached selectors can not be null...");

		List<Document> documents = null;

		if (extractor.getFilters() == null && extractor.getFilters().getForLinks() == null || extractor.getFilters().getForLinks() != null
				&& extractor.getFilters().getForLinks().stream().allMatch(f -> f.isValid(new Link().uri(blob.metadata().uri())))) {

			if (extractor.getMultiple() != null) {

				documents = new ArrayList<>();

				// Extract the multiple
				List<String> segments = extract(cachedSelectors, blob, null, extractor.getMultiple(), DataConverter.BODY);

				documents = segments.stream().map(segment -> {

					Document document = new Document(extractor.getFields().size());

					for (FieldExtractor field : extractor.getFields()) {

						// Extract the value
						List<? extends Object> results = null;

						boolean isBody = SourceType.BODY.equals(field.getExtractor().getSource());
						if (field.isUseMultiple() && isBody) {
							results = extract(cachedSelectors, blob, segment.getBytes(Charsets.UTF_8), field.getExtractor(), DataConverter.BODY);
						} else {
							DataConverter<?, String> converter = isBody ? DataConverter.BODY : DataConverter.DEFAULT;
							results = extract(cachedSelectors, blob, null, field.getExtractor(), converter);
						}

						fillDocument(blob.metadata(), document, field, results);
					}

					return document;
				}).collect(Collectors.toList());

			} else {

				Document document = new Document(extractor.getFields().size());

				for (FieldExtractor field : extractor.getFields()) {

					// Extract the value
					boolean isBody = SourceType.BODY.equals(field.getExtractor().getSource());
					DataConverter<?, String> converter = isBody ? DataConverter.BODY : DataConverter.DEFAULT;
					List<? extends Object> results = extract(cachedSelectors, blob, null, field.getExtractor(), converter);

					fillDocument(blob.metadata(), document, field, results);
				}

				documents = Arrays.asList(document);

			}
		}
		return documents;
	}

	public void fillDocument(BlobMetadata data, Document document, FieldExtractor field, List<? extends Object> results) {
		if (results != null && !results.isEmpty()) {

			if (field.isFirstOnly()) {
				results = results.subList(0, 1);
			}

			results = format(data, field, results);

			// Add it
			document.put(field.getName(), results);
		}
	}

	public <T, U> List<U> extract(Map<String, Instance<?>> selectors, Blob blob, byte[] segment, Extractor fieldExtractor, DataConverter<T, U> converter) {
		Preconditions.checkNotNull(fieldExtractor, "There is no field extractor...");
		Preconditions.checkNotNull(fieldExtractor.getType(), "Extractor without type");
		// Preconditions.checkNotNull(fieldExtractor.getValue(),
		// "Extractor without value");

		Instance<T> instance;
		if (segment != null) {
			Selector<T> selector = getSelector(fieldExtractor);
			instance = ((BodySelector<T>) selector).init(blob.metadata(), Payloads.newByteArrayPayload(segment), true);
		} else {
			// Reuse the previous instance selector for this web page
			String cacheKey = fieldExtractor.getType() + "-" + fieldExtractor.getSource().toString().toLowerCase(Locale.ROOT);
			instance = (Instance<T>) selectors.get(cacheKey);

			if (instance == null) {
				Selector<T> selector = getSelector(fieldExtractor);

				if (SourceType.BODY.equals(fieldExtractor.getSource())) {
					instance = ((BodySelector<T>) selector).init(blob.metadata(), blob.payload(), false);
				} else if (SourceType.HEADERS.equals(fieldExtractor.getSource())) {
					instance = ((HeaderSelector<T>) selector).init(blob.metadata().fetchMetadata());
				} else if (SourceType.URL.equals(fieldExtractor.getSource())) {
					instance = ((UrlSelector<T>) selector).init(blob.metadata(), blob.metadata().uri());
				} else if (SourceType.COOKIE.equals(fieldExtractor.getSource())) {
					instance = ((CookieSelector<T>) selector).init(blob.metadata().fetchMetadata());
				} else if (SourceType.EMPTY.equals(fieldExtractor.getSource())) {
					instance = ((EmptySelector<T>) selector).init(blob);
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

	public List<? extends Object> format(BlobMetadata data, NamedDataExtractorFormatter dataExtractorFormatter, List<? extends Object> results) {
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
