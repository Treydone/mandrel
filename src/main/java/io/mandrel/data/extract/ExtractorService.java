package io.mandrel.data.extract;

import io.mandrel.data.content.Extractor;
import io.mandrel.data.content.FieldExtractor;
import io.mandrel.data.content.Formatter;
import io.mandrel.data.content.NamedDataExtractorFormatter;
import io.mandrel.data.content.OutlinkExtractor;
import io.mandrel.data.content.SourceType;
import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.data.content.selector.BodySelector;
import io.mandrel.data.content.selector.CookieSelector;
import io.mandrel.data.content.selector.DataConverter;
import io.mandrel.data.content.selector.EmptySelector;
import io.mandrel.data.content.selector.HeaderSelector;
import io.mandrel.data.content.selector.Selector;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.data.content.selector.SelectorService;
import io.mandrel.data.content.selector.UrlSelector;
import io.mandrel.data.spider.Link;
import io.mandrel.gateway.Document;
import io.mandrel.http.Cookie;
import io.mandrel.http.WebPage;
import io.mandrel.script.ScriptingService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import us.codecraft.xsoup.xevaluator.XElement;

import com.google.common.base.Preconditions;

@Component
@Slf4j
public class ExtractorService {

	private final ScriptingService scriptingService;

	private final SelectorService selectorService;

	@Inject
	public ExtractorService(ScriptingService scriptingService, SelectorService selectorService) {
		super();
		this.scriptingService = scriptingService;
		this.selectorService = selectorService;
	}

	public Set<Link> extractOutlinks(WebPage webPage, OutlinkExtractor extractor) {
		Map<String, Instance<?>> cachedSelectors = new HashMap<>();

		List<Link> outlinks = extract(cachedSelectors, webPage, null, extractor.getExtractor(), new DataConverter<XElement, Link>() {
			public Link convert(XElement element) {
				Link link = new Link();
				link.setUri(element.getElement().attr("href"));
				link.setRel(element.getElement().attr("rel"));
				link.setTitle(element.getElement().attr("title"));
				link.setText(element.getElement().val());
				return link;
			}
		});

		if (outlinks != null && !outlinks.isEmpty()) {
			outlinks = (List<Link>) format(webPage, extractor, outlinks);
		}

		return new HashSet<>(outlinks);
	}

	public void extractThenFormatThenStore(long spiderId, WebPage webPage, WebPageExtractor extractor) {

		List<Document> documents = extractThenFormat(webPage, extractor);

		// Store the result
		if (documents != null) {
			extractor.getDataStore().save(spiderId, documents);
		}
	}

	public List<Document> extractThenFormat(WebPage webPage, WebPageExtractor extractor) {
		Preconditions.checkNotNull(extractor.getFields(), "No field for this extractor...");
		Preconditions.checkNotNull(extractor.getDataStore(), "No datastore for this extractor...");

		List<Document> documents = null;

		if (extractor.getFilters() == null || extractor.getFilters().stream().anyMatch(f -> f.isValid(webPage))) {
			Map<String, Instance<?>> cachedSelectors = new HashMap<>();

			if (extractor.getMultiple() != null) {

				documents = new ArrayList<>();

				// Extract the multiple
				List<String> segments = extract(cachedSelectors, webPage, null, extractor.getMultiple());

				documents = segments.stream().map(segment -> {

					Document document = new Document(extractor.getFields().size());

					for (FieldExtractor field : extractor.getFields()) {

						// Extract the value
						List<? extends Object> results = null;

						if (field.isUseMultiple() && SourceType.BODY.equals(field.getExtractor().getSource())) {
							results = extract(cachedSelectors, webPage, new ByteArrayInputStream(segment.getBytes()), field.getExtractor());
						} else {
							results = extract(cachedSelectors, webPage, null, field.getExtractor());
						}

						if (results != null && !results.isEmpty()) {
							results = format(webPage, field, results);

							// Add it
							document.put(field.getName(), results);
						}
					}

					return document;
				}).collect(Collectors.toList());

			} else {

				Document document = new Document(extractor.getFields().size());

				for (FieldExtractor field : extractor.getFields()) {

					// Extract the value
					List<? extends Object> results = extract(cachedSelectors, webPage, null, field.getExtractor());

					if (results != null && !results.isEmpty()) {

						results = format(webPage, field, results);
						// Add it
						document.put(field.getName(), results);
					}
				}

				documents = Arrays.asList(document);

			}
		}
		return documents;
	}

	public List<String> extract(Map<String, Instance<?>> selectors, WebPage webPage, InputStream segment, Extractor fieldExtractor) {
		return extract(selectors, webPage, segment, fieldExtractor, DataConverter.BODY);
	}

	public <T, U> List<U> extract(Map<String, Instance<?>> selectors, WebPage webPage, InputStream segment, Extractor fieldExtractor,
			DataConverter<T, U> converter) {
		Preconditions.checkNotNull(fieldExtractor, "There is no field extractor...");
		Preconditions.checkNotNull(fieldExtractor.getType(), "Extractor without type");
		Preconditions.checkNotNull(fieldExtractor.getValue(), "Extractor without value");

		Instance<T> instance;
		if (segment != null) {
			Selector<T> selector = getSelector(fieldExtractor);
			instance = ((BodySelector) selector).init(webPage, segment, true);
		} else {
			// Reuse the previous instance selector for this web page
			String cacheKey = fieldExtractor.getType() + "-" + fieldExtractor.getSource().toString().toLowerCase();
			instance = (Instance<T>) selectors.get(cacheKey);

			if (instance == null) {
				Selector selector = getSelector(fieldExtractor);

				if (SourceType.BODY.equals(fieldExtractor.getSource())) {
					instance = ((BodySelector) selector).init(webPage, webPage.getBody(), false);
				} else if (SourceType.HEADERS.equals(fieldExtractor.getSource())) {
					instance = ((HeaderSelector) selector).init(webPage, webPage.getMetadata().getHeaders());
				} else if (SourceType.URL.equals(fieldExtractor.getSource())) {
					instance = ((UrlSelector) selector).init(webPage, webPage.getUrl());
				} else if (SourceType.COOKIE.equals(fieldExtractor.getSource())) {
					instance = ((CookieSelector) selector).init(
							webPage,
							webPage.getMetadata()
									.getCookies()
									.stream()
									.map(cookie -> new Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getExpires(),
											cookie.getMaxAge(), cookie.isSecure(), cookie.isHttpOnly())).collect(Collectors.toList()));
				} else if (SourceType.EMPTY.equals(fieldExtractor.getSource())) {
					instance = ((EmptySelector) selector).init(webPage);
				}

				selectors.put(cacheKey, instance);
			}
		}

		List<U> result = instance.select(fieldExtractor.getValue(), converter);
		return result;
	}

	private Selector getSelector(Extractor fieldExtractor) {
		Selector selector = selectorService.getSelectorByName(fieldExtractor.getType());
		if (selector == null) {
			throw new IllegalArgumentException("Unknown extractor '" + fieldExtractor.getType() + "'");
		}
		return selector;
	}

	public List<? extends Object> format(WebPage webPage, NamedDataExtractorFormatter dataExtractorFormatter, List<? extends Object> results) {
		// ... and format it if necessary
		Formatter formatter = dataExtractorFormatter.getFormatter();
		if (formatter != null) {
			// TODO: optimize by reusing the previous engine if it is
			// the same scripting language
			ScriptEngine engine = scriptingService.getEngineByName(formatter.getType());

			return results.stream().map(result -> {
				ScriptContext bindings = scriptingService.getBindings(webPage, result);
				try {
					return scriptingService.execScript(new String(formatter.getValue()), engine, bindings);
				} catch (Exception e) {
					log.debug("Can not format '{}': {}", dataExtractorFormatter.getName(), e);
				}
				return null;
			}).collect(Collectors.toList());

		}
		return results;
	}

}
