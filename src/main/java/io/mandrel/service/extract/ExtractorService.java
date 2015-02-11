package io.mandrel.service.extract;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.Extractor;
import io.mandrel.common.content.FieldExtractor;
import io.mandrel.common.content.Formatter;
import io.mandrel.common.content.NamedDataExtractorFormatter;
import io.mandrel.common.content.OutlinkExtractor;
import io.mandrel.common.content.WebPageExtractor;
import io.mandrel.common.content.selector.SelectorService;
import io.mandrel.common.content.selector.WebPageSelector;
import io.mandrel.common.content.selector.WebPageSelector.Instance;
import io.mandrel.common.script.ScriptingService;
import io.mandrel.common.store.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

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

	public List<String> extractOutlinks(WebPage webPage, OutlinkExtractor extractor) {
		Map<String, Instance> cachedSelectors = new HashMap<String, Instance>();

		List<String> outlinks = extract(cachedSelectors, webPage, null, extractor.getExtractor());

		if (outlinks != null && !outlinks.isEmpty()) {
			outlinks = (List<String>) format(webPage, extractor, outlinks);
		}

		return outlinks;
	}

	public void extractFormatThenStore(WebPage webPage, WebPageExtractor extractor) {

		Preconditions.checkNotNull(extractor.getFields(), "No field for this extractor...");
		Preconditions.checkNotNull(extractor.getDataStore(), "No datastore for this extractor...");

		if (extractor.getFilters() == null || extractor.getFilters().stream().anyMatch(f -> f.isValid(webPage))) {
			Map<String, Instance> cachedSelectors = new HashMap<String, Instance>();

			if (extractor.getMultiple() != null) {

				List<Document> documents = new ArrayList<>();

				// Extract the multiple
				List<String> segments = extract(cachedSelectors, webPage, null, extractor.getMultiple());

				segments.forEach(segment -> {

					Document document = new Document(extractor.getFields().size());

					for (FieldExtractor field : extractor.getFields()) {

						// Extract the value
						List<? extends Object> results = null;

						if (field.isUseMultiple()) {
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

					documents.add(document);
				});

				// Store the result
				extractor.getDataStore().save(documents);

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

				// Store the result
				extractor.getDataStore().save(document);
			}
		}
	}

	public List<String> extract(Map<String, Instance> selectors, WebPage webPage, InputStream segment, Extractor fieldExtractor) {
		Preconditions.checkNotNull(fieldExtractor, "There is no field extractor...");
		Preconditions.checkNotNull(fieldExtractor.getType(), "Extractor without type");
		Preconditions.checkNotNull(fieldExtractor.getValue(), "Extractor without value");

		Instance instance;
		if (segment != null) {
			WebPageSelector selector = getSelector(fieldExtractor);
			instance = selector.init(webPage, segment);
		} else {
			// Reuse the previous instance selector for this web page
			instance = selectors.get(fieldExtractor.getType());
			if (instance == null) {
				WebPageSelector selector = getSelector(fieldExtractor);
				instance = selector.init(webPage, webPage.getBody());
				selectors.put(fieldExtractor.getType(), instance);
			}
		}

		List<String> result = instance.select(fieldExtractor.getValue());
		return result;
	}

	private WebPageSelector getSelector(Extractor fieldExtractor) {
		WebPageSelector selector = selectorService.getSelectorByName(fieldExtractor.getType());
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
