package io.mandrel.data.extract;

import io.mandrel.common.data.Spider;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

	public Pair<Set<Link>, Set<String>> extractAndFilterOutlinks(Spider spider, String url, Map<String, Instance<?>> cachedSelectors, WebPage webPage,
			OutlinkExtractor ol) {
		// Find outlinks in page
		Set<Link> outlinks = extractOutlinks(cachedSelectors, webPage, ol);
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
			allFilteredOutlinks = spider.getStores().getPageMetadataStore().filter(spider.getId(), filteredOutlinks, spider.getClient().getPoliteness());
		}
		log.trace("And filtering {}", filteredOutlinks);
		return Pair.of(outlinks, allFilteredOutlinks);
	}

	public Set<Link> extractOutlinks(Map<String, Instance<?>> cachedSelectors, WebPage webPage, OutlinkExtractor extractor) {

		List<Link> outlinks = extract(cachedSelectors, webPage, null, extractor.getExtractor(), new DataConverter<XElement, Link>() {
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
			outlinks = (List<Link>) format(webPage, extractor, outlinks);
		}

		return new HashSet<>(outlinks);
	}

	public List<Document> extractThenFormatThenStore(long spiderId, Map<String, Instance<?>> cachedSelectors, WebPage webPage, WebPageExtractor extractor) {

		List<Document> documents = extractThenFormat(cachedSelectors, webPage, extractor);

		// Store the result
		if (documents != null) {
			extractor.getDocumentStore().save(spiderId, documents);
		}
		
		return documents;
	}

	public List<Document> extractThenFormat(Map<String, Instance<?>> cachedSelectors, WebPage webPage, WebPageExtractor extractor) {
		Preconditions.checkNotNull(extractor.getFields(), "No field for this extractor...");
		Preconditions.checkNotNull(extractor.getDocumentStore(), "No datastore for this extractor...");
		Preconditions.checkNotNull(cachedSelectors, "Cached selectors can not be null...");

		List<Document> documents = null;

		if (extractor.getFilters() == null && extractor.getFilters().getForLinks() == null || extractor.getFilters().getForLinks() != null
				&& extractor.getFilters().getForLinks().stream().allMatch(f -> f.isValid(new Link().setUri(webPage.getUrl().toString())))) {

			if (extractor.getMultiple() != null) {

				documents = new ArrayList<>();

				// Extract the multiple
				List<String> segments = extract(cachedSelectors, webPage, null, extractor.getMultiple(), DataConverter.BODY);

				documents = segments.stream().map(segment -> {

					Document document = new Document(extractor.getFields().size());

					for (FieldExtractor field : extractor.getFields()) {

						// Extract the value
						List<? extends Object> results = null;

						boolean isBody = SourceType.BODY.equals(field.getExtractor().getSource());
						if (field.isUseMultiple() && isBody) {
							results = extract(cachedSelectors, webPage, segment.getBytes(), field.getExtractor(), DataConverter.BODY);
						} else {
							DataConverter<?, String> converter = isBody ? DataConverter.BODY : DataConverter.DEFAULT;
							results = extract(cachedSelectors, webPage, null, field.getExtractor(), converter);
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
					boolean isBody = SourceType.BODY.equals(field.getExtractor().getSource());
					DataConverter<?, String> converter = isBody ? DataConverter.BODY : DataConverter.DEFAULT;
					List<? extends Object> results = extract(cachedSelectors, webPage, null, field.getExtractor(), converter);

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

	public <T, U> List<U> extract(Map<String, Instance<?>> selectors, WebPage webPage, byte[] segment, Extractor fieldExtractor, DataConverter<T, U> converter) {
		Preconditions.checkNotNull(fieldExtractor, "There is no field extractor...");
		Preconditions.checkNotNull(fieldExtractor.getType(), "Extractor without type");
		// Preconditions.checkNotNull(fieldExtractor.getValue(),
		// "Extractor without value");

		Instance<T> instance;
		if (segment != null) {
			Selector<T> selector = getSelector(fieldExtractor);
			instance = ((BodySelector<T>) selector).init(webPage, segment, true);
		} else {
			// Reuse the previous instance selector for this web page
			String cacheKey = fieldExtractor.getType() + "-" + fieldExtractor.getSource().toString().toLowerCase();
			instance = (Instance<T>) selectors.get(cacheKey);

			if (instance == null) {
				Selector<T> selector = getSelector(fieldExtractor);

				if (SourceType.BODY.equals(fieldExtractor.getSource())) {
					instance = ((BodySelector<T>) selector).init(webPage, webPage.getBody(), false);
				} else if (SourceType.HEADERS.equals(fieldExtractor.getSource())) {
					instance = ((HeaderSelector<T>) selector).init(webPage, webPage.getMetadata().getHeaders());
				} else if (SourceType.URL.equals(fieldExtractor.getSource())) {
					instance = ((UrlSelector<T>) selector).init(webPage, webPage.getUrl());
				} else if (SourceType.COOKIE.equals(fieldExtractor.getSource())) {
					instance = ((CookieSelector<T>) selector).init(
							webPage,
							webPage.getMetadata()
									.getCookies()
									.stream()
									.map(cookie -> new Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getExpires(),
											cookie.getMaxAge(), cookie.isSecure(), cookie.isHttpOnly())).collect(Collectors.toList()));
				} else if (SourceType.EMPTY.equals(fieldExtractor.getSource())) {
					instance = ((EmptySelector<T>) selector).init(webPage);
				}

				selectors.put(cacheKey, instance);
			}
		}

		return instance.select(fieldExtractor.getValue(), converter);
	}

	private <T> Selector<T> getSelector(Extractor fieldExtractor) {
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
