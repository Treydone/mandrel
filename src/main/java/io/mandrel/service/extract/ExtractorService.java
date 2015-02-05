package io.mandrel.service.extract;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.Field;
import io.mandrel.common.content.FieldExtractor;
import io.mandrel.common.content.FieldFormatter;
import io.mandrel.common.content.WebPageExtractor;
import io.mandrel.common.content.selector.SelectorService;
import io.mandrel.common.content.selector.WebPageSelector;
import io.mandrel.common.content.selector.WebPageSelector.Instance;
import io.mandrel.common.script.ScriptingService;
import io.mandrel.common.store.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
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
	public ExtractorService(ScriptingService scriptingService,
			SelectorService selectorService) {
		super();
		this.scriptingService = scriptingService;
		this.selectorService = selectorService;
	}

	public void extractFormatThenStore(WebPage webPage,
			WebPageExtractor extractor) {

		Preconditions.checkNotNull(extractor.getMatchingPatterns(),
				"Not matching pattern for this extractor...");
		Preconditions.checkNotNull(extractor.getFields(),
				"No field for this extractor...");
		Preconditions.checkNotNull(extractor.getDataStore(),
				"No datastore for this extractor...");

		Optional<Pattern> optionnalMatch = extractor
				.getMatchingPatterns()
				.stream()
				.filter(pattern -> pattern.matcher(webPage.getUrl().toString())
						.matches()).findFirst();

		if (optionnalMatch.isPresent()) {
			Map<String, Instance> cachedSelectors = new HashMap<String, Instance>();

			Document document = new Document(extractor.getFields().size());

			for (Field field : extractor.getFields()) {

				// Extract the value
				List<Object> results = extract(cachedSelectors, webPage, field);

				if (results != null) {
					results = format(webPage, field, results);
				}

				// Add it
				document.put(field.getName(), results);
			}

			// Store the result
			extractor.getDataStore().save(document);
		}
	}

	public List<Object> extract(Map<String, Instance> selectors,
			WebPage webPage, Field field) {
		FieldExtractor fieldExtractor = field.getExtractor();
		Preconditions.checkNotNull(fieldExtractor,
				"There is no field extractor...");
		Preconditions.checkNotNull(fieldExtractor.getType(),
				"Extractor without type");
		Preconditions.checkNotNull(fieldExtractor.getValue(),
				"Extractor without value");

		// Reuse the previous instance selector for this web page
		Instance instance = selectors.get(fieldExtractor.getType());
		if (instance == null) {
			WebPageSelector selector = selectorService
					.getSelectorByName(fieldExtractor.getType());
			if (selector == null) {
				throw new IllegalArgumentException("Unknown extractor '"
						+ fieldExtractor.getType() + "'");
			}
			instance = selector.init(webPage);
			selectors.put(fieldExtractor.getType(), instance);
		}

		List<Object> result = instance.select(fieldExtractor.getValue());
		return result;
	}

	public List<Object> format(WebPage webPage, Field field,
			List<Object> results) {
		// ... and format it if necessary
		FieldFormatter formatter = field.getFormatter();
		if (formatter != null) {
			// TODO: optimize by reusing the previous engine if it is
			// the same scripting language
			ScriptEngine engine = scriptingService.getEngineByName(formatter
					.getType());

			return results
					.stream()
					.map(result -> {
						ScriptContext bindings = scriptingService.getBindings(
								webPage, result);
						try {
							return scriptingService.execScript(new String(
									formatter.getValue()), engine, bindings);
						} catch (Exception e) {
							log.debug("Can not format field {}: {}",
									field.getName(), e);
						}
						return null;
					}).collect(Collectors.toList());

		}
		return results;
	}

}
