package io.mandrel.common.content;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.selector.SelectorService;
import io.mandrel.common.content.selector.WebPageSelector;
import io.mandrel.common.content.selector.WebPageSelector.Instance;
import io.mandrel.common.script.ScriptingService;
import io.mandrel.common.store.DataStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class WebPageExtractor {

	private String name;

	private DataStore dataStore;

	private List<String> matchingPatternsAsString;

	private List<Pattern> matchingPatterns;

	private List<Field> fields;

	private ScriptingService scriptingService;

	private SelectorService selectorService;

	public void extractFormatThenStore(WebPage webPage) {

		Optional<Pattern> optionnalMatch = matchingPatterns
				.stream()
				.filter(pattern -> pattern.matcher(webPage.getUrl().toString())
						.matches()).findFirst();

		if (optionnalMatch.isPresent()) {
			Map<String, Instance> cachedSelectors = new HashMap<String, Instance>();

			Map<String, List<Object>> data = new HashMap<String, List<Object>>(
					fields.size());

			for (Field field : fields) {

				// Extract the value
				List<Object> results = extract(cachedSelectors, webPage, field);

				if (results != null) {
					results = format(webPage, field, results);
				}

				// Add it
				data.put(field.getName(), results);
			}

			// Store the result
			dataStore.save(data);
		}
	}

	public List<Object> extract(Map<String, Instance> selectors,
			WebPage webPage, Field field) {
		FieldExtractor extractor = field.getExtractor();
		if (extractor.getType() == null) {
			throw new IllegalArgumentException("Extractor without type");
		}

		// Reuse the previous instance selector for this web page
		Instance instance = selectors.get(extractor.getType());
		if (instance == null) {
			WebPageSelector selector = selectorService
					.getSelectorByName(extractor.getType());
			if (selector == null) {
				throw new IllegalArgumentException("Unknown extractor '"
						+ extractor.getType() + "'");
			}
			instance = selector.init(webPage);
			selectors.put(extractor.getType(), instance);
		}

		List<Object> result = instance.select(extractor.getValue());
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

	public void setMatchingPatternsAsString(
			List<String> matchingPatternsAsString) {
		this.matchingPatternsAsString = matchingPatternsAsString;
		matchingPatterns = this.matchingPatternsAsString.stream()
				.map(pattern -> Pattern.compile(pattern))
				.collect(Collectors.toList());
	}

}
