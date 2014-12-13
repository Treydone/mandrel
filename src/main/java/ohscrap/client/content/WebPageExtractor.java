package ohscrap.client.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import ohscrap.client.content.selector.SelectorService;
import ohscrap.client.content.selector.WebPageSelector;
import ohscrap.client.script.ScriptingService;
import ohscrap.common.WebPage;
import ohscrap.store.DataStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebPageExtractor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WebPageExtractor.class);

	private String name;

	private DataStore dataStore;

	private List<String> matchingPatterns;

	private List<Field> fields;

	private ScriptingService scriptingService;

	private SelectorService selectorService;

	public void extractFormatThenStore(WebPage webPage) {

		Map<String, Object> data = new HashMap<String, Object>(fields.size());

		for (Field field : fields) {

			// Extract the value
			Object result = extract(field);

			if (result != null) {
				result = format(webPage, field, result);
			}

			// Add it
			data.put(field.getName(), result);
		}

		// Store the result
		dataStore.save(data);
	}

	public Object extract(Field field) {
		FieldExtractor extractor = field.getExtractor();
		if (extractor.getType() == null) {
			throw new IllegalArgumentException("Extractor without type");
		}

		WebPageSelector selector = selectorService.getSelectorByName(extractor
				.getType());
		if (selector == null) {
			throw new IllegalArgumentException("Unknown extractor '"
					+ extractor.getType() + "'");
		}
		Object result = selector.select(extractor.getValue());
		return result;
	}

	public Object format(WebPage webPage, Field field, Object result) {
		// ... and format it if necessary
		FieldFormatter formatter = field.getFormatter();
		if (formatter != null) {
			ScriptContext bindings = scriptingService.getBindings(webPage,
					result);
			// TODO: optimize by reusing the previous engine if it is
			// the
			// same scripting language
			ScriptEngine engine = scriptingService.getEngineByName(formatter
					.getType());
			try {
				result = scriptingService.execScript(
						new String(formatter.getValue()), engine, bindings);
			} catch (Exception e) {
				LOGGER.debug("Can not format field {}: {}", field.getName(), e);
			}
		}
		return result;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataStore getDataStore() {
		return dataStore;
	}

	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public List<String> getMatchingPatterns() {
		return matchingPatterns;
	}

	public void setMatchingPatterns(List<String> matchingPatterns) {
		this.matchingPatterns = matchingPatterns;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	public ScriptingService getScriptingService() {
		return scriptingService;
	}

	public void setScriptingService(ScriptingService scriptingService) {
		this.scriptingService = scriptingService;
	}

	public SelectorService getSelectorService() {
		return selectorService;
	}

	public void setSelectorService(SelectorService selectorService) {
		this.selectorService = selectorService;
	}

}
