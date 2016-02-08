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
package io.mandrel.data.content;

import io.mandrel.blob.Blob;
import io.mandrel.blob.BlobMetadata;
import io.mandrel.common.loader.NamedProviders;
import io.mandrel.data.Link;
import io.mandrel.data.content.selector.BodySelector;
import io.mandrel.data.content.selector.CookieSelector;
import io.mandrel.data.content.selector.DataConverter;
import io.mandrel.data.content.selector.EmptySelector;
import io.mandrel.data.content.selector.HeaderSelector;
import io.mandrel.data.content.selector.Selector;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.data.content.selector.UriSelector;
import io.mandrel.document.Document;
import io.mandrel.io.Payloads;
import io.mandrel.script.ScriptingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

@Slf4j
@Data
public class DefaultDataExtractor extends DataExtractor {

	@JsonProperty("multiple")
	protected Extractor multiple;

	@JsonProperty("fields")
	protected List<FieldExtractor> fields;

	@JsonProperty("key_field")
	protected String keyField;

	@Override
	public List<Document> extract(ScriptingService engine, Map<String, Instance<?>> cachedSelectors, Blob blob) {
		Preconditions.checkNotNull(fields, "No field for this ..");
		Preconditions.checkNotNull(documentStore, "No datastore for this ..");
		Preconditions.checkNotNull(cachedSelectors, "Cached selectors can not be null...");

		List<Document> documents = null;

		if (filters == null && filters.getLinks() == null || filters.getLinks() != null
				&& filters.getLinks().stream().allMatch(f -> f.isValid(new Link().setUri(blob.getMetadata().getUri())))) {

			if (multiple != null) {

				documents = new ArrayList<>();

				// Extract the multiple
				List<String> segments = extract(cachedSelectors, blob, null, multiple, DataConverter.BODY);

				documents = segments.stream().map(segment -> {

					Document document = new Document(fields.size());

					for (FieldExtractor field : fields) {

						// Extract the value
						List<? extends Object> results = null;

						boolean isBody = SourceType.BODY.equals(field.getExtractor().getSource());
						if (field.isUseMultiple() && isBody) {
							results = extract(cachedSelectors, blob, segment.getBytes(Charsets.UTF_8), field.getExtractor(), DataConverter.BODY);
						} else {
							DataConverter<?, String> converter = isBody ? DataConverter.BODY : DataConverter.DEFAULT;
							results = extract(cachedSelectors, blob, null, field.getExtractor(), converter);
						}

						fillDocument(engine, blob.getMetadata(), document, field, results);
					}

					return document;
				}).collect(Collectors.toList());

			} else {

				Document document = new Document(fields.size());

				for (FieldExtractor field : fields) {

					// Extract the value
					boolean isBody = SourceType.BODY.equals(field.getExtractor().getSource());
					DataConverter<?, String> converter = isBody ? DataConverter.BODY : DataConverter.DEFAULT;
					List<? extends Object> results = extract(cachedSelectors, blob, null, field.getExtractor(), converter);

					fillDocument(engine, blob.getMetadata(), document, field, results);
				}

				documents = Arrays.asList(document);

			}
		}
		return documents;
	}

	public void fillDocument(ScriptingService engine, BlobMetadata data, Document document, FieldExtractor field, List<? extends Object> results) {
		if (results != null && !results.isEmpty()) {

			if (field.isFirstOnly()) {
				results = results.subList(0, 1);
			}

			results = format(engine, data, field, results);

			// Add it
			document.put(field.getName(), results);
		}
	}

	public static <T, U> List<U> extract(Map<String, Instance<?>> selectors, Blob blob, byte[] segment, Extractor fieldExtractor, DataConverter<T, U> converter) {
		Preconditions.checkNotNull(fieldExtractor, "There is no field extractor...");
		Preconditions.checkNotNull(fieldExtractor.getType(), "Extractor without type");
		// Preconditions.checkNotNull(fieldExtractor.getValue(),
		// "Extractor without value");

		Instance<T> instance;
		if (segment != null) {
			Selector<T> selector = getSelector(fieldExtractor);
			instance = ((BodySelector<T>) selector).init(blob.getMetadata(), Payloads.newByteArrayPayload(segment), true);
		} else {
			// Reuse the previous instance selector for this web page
			String cacheKey = fieldExtractor.getType() + "-" + fieldExtractor.getSource().toString().toLowerCase(Locale.ROOT);
			instance = (Instance<T>) selectors.get(cacheKey);

			if (instance == null) {
				Selector<T> selector = getSelector(fieldExtractor);

				if (SourceType.BODY.equals(fieldExtractor.getSource())) {
					instance = ((BodySelector<T>) selector).init(blob.getMetadata(), blob.getPayload(), false);
				} else if (SourceType.HEADERS.equals(fieldExtractor.getSource())) {
					instance = ((HeaderSelector<T>) selector).init(blob.getMetadata().getFetchMetadata());
				} else if (SourceType.URI.equals(fieldExtractor.getSource())) {
					instance = ((UriSelector<T>) selector).init(blob.getMetadata(), blob.getMetadata().getUri());
				} else if (SourceType.COOKIE.equals(fieldExtractor.getSource())) {
					instance = ((CookieSelector<T>) selector).init(blob.getMetadata().getFetchMetadata());
				} else if (SourceType.EMPTY.equals(fieldExtractor.getSource())) {
					instance = ((EmptySelector<T>) selector).init(blob);
				}

				selectors.put(cacheKey, instance);
			}
		}

		return instance.select(fieldExtractor.getValue(), converter);
	}

	private static <T> Selector<T> getSelector(Extractor fieldExtractor) {
		Selector selector = NamedProviders.get(Selector.class, fieldExtractor.getType());
		if (selector == null) {
			throw new IllegalArgumentException("Unknown extractor '" + fieldExtractor.getType() + "'");
		}
		return selector;
	}

	public static List<? extends Object> format(ScriptingService service, BlobMetadata data, NamedDataExtractorFormatter dataExtractorFormatter,
			List<? extends Object> results) {
		// ... and format it if necessary
		Formatter formatter = dataExtractorFormatter.getFormatter();
		if (formatter != null) {
			// TODO: optimize by reusing the previous engine if it is
			// the same scripting language

			ScriptEngine engine = service.getEngineByName(formatter.getType());

			return results.stream().map(result -> {
				ScriptContext bindings = service.getBindings(data, result);
				try {
					return service.execScript(new String(formatter.getValue(), Charsets.UTF_8), engine, bindings);
				} catch (Exception e) {
					log.debug("Can not format '{}': {}", dataExtractorFormatter.getName(), e);
				}
				return null;
			}).collect(Collectors.toList());

		}
		return results;
	}

	@Override
	public String name() {
		return "default";
	}
}
