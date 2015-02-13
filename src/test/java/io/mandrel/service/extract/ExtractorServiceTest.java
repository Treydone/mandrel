package io.mandrel.service.extract;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.FieldExtractor;
import io.mandrel.common.content.Extractor;
import io.mandrel.common.content.SourceType;
import io.mandrel.common.content.WebPageExtractor;
import io.mandrel.common.content.selector.SelectorService;
import io.mandrel.common.script.ScriptingService;
import io.mandrel.common.store.Document;
import io.mandrel.common.store.DocumentStore;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExtractorServiceTest {

	@Mock
	private ScriptingService scriptingService;

	@Mock
	private DocumentStore dataStore;

	private SelectorService selectorService = new SelectorService();

	private ExtractorService extractorService;

	@Before
	public void init() {
		extractorService = new ExtractorService(scriptingService, selectorService);
	}

	@Test(expected = NullPointerException.class)
	public void no_matching_pattern() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_datastore() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();

		FieldExtractor field = new FieldExtractor();
		field.setName("date");
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field_extractor() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();

		extractor.setDataStore(dataStore);
		FieldExtractor field = new FieldExtractor();
		field.setName("date");
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field_extractor_type() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();

		extractor.setDataStore(dataStore);
		FieldExtractor field = new FieldExtractor();
		field.setName("date");
		Extractor fieldExtractor = new Extractor();
		fieldExtractor.setValue("");
		field.setExtractor(fieldExtractor);
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field_extractor_value() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();

		extractor.setDataStore(dataStore);
		FieldExtractor field = new FieldExtractor();
		field.setName("date");
		Extractor fieldExtractor = new Extractor();
		fieldExtractor.setType("xpath");
		field.setExtractor(fieldExtractor);
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test
	public void simple() throws MalformedURLException {

		// Arrange
		ByteArrayInputStream stream = new ByteArrayInputStream(
				"<html><test><o>value1</o><t>key1</t></test><test><o>value2</o></test></html>".getBytes());

		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, stream);
		WebPageExtractor extractor = new WebPageExtractor();

		extractor.setDataStore(dataStore);
		FieldExtractor field = new FieldExtractor();
		field.setName("date");
		Extractor fieldExtractor = new Extractor();
		fieldExtractor.setType("xpath");
		fieldExtractor.setSource(SourceType.BODY);
		fieldExtractor.setValue("//test/o/text()");
		field.setExtractor(fieldExtractor);
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
		Document data = new Document();
		data.put("date", Arrays.asList("value1", "value2"));
		Mockito.verify(dataStore).save(data);
	}

	@Test
	public void simple_with_mutiple_extractors() throws MalformedURLException {

		// Arrange
		ByteArrayInputStream stream = new ByteArrayInputStream(
				"<html><test><o>value1</o><t>key1</t></test><test><o>value2</o></test></html>".getBytes());

		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, stream);
		WebPageExtractor extractor = new WebPageExtractor();

		extractor.setDataStore(dataStore);

		FieldExtractor dateField = new FieldExtractor();
		dateField.setName("date");
		Extractor dateFieldExtractor = new Extractor();
		dateFieldExtractor.setType("xpath");
		dateFieldExtractor.setSource(SourceType.BODY);
		dateFieldExtractor.setValue("//test/o/text()");
		dateField.setExtractor(dateFieldExtractor);

		FieldExtractor keyField = new FieldExtractor();
		keyField.setName("key");
		Extractor keyFieldExtractor = new Extractor();
		keyFieldExtractor.setType("xpath");
		keyFieldExtractor.setSource(SourceType.BODY);
		keyFieldExtractor.setValue("//test/t/text()");
		keyField.setExtractor(keyFieldExtractor);

		extractor.setFields(Arrays.asList(dateField, keyField));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
		Document data = new Document();
		data.put("date", Arrays.asList("value1", "value2"));
		data.put("key", Arrays.asList("key1"));
		Mockito.verify(dataStore).save(data);
	}

	@Test
	public void multiple() throws MalformedURLException {

		// Arrange
		ByteArrayInputStream stream = new ByteArrayInputStream(
				"<!--?xml version=\"1.0\"?--><html><body><test><o>value1</o><t>key1</t></test><test><o>value2</o><t>key2</t></test><test><o>value3</o></test></body></html>"
						.getBytes());

		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, stream);
		WebPageExtractor extractor = new WebPageExtractor();
		extractor.setDataStore(dataStore);

		FieldExtractor dateField = new FieldExtractor();
		dateField.setName("date");
		Extractor dateFieldExtractor = new Extractor();
		dateFieldExtractor.setType("xpath");
		dateFieldExtractor.setSource(SourceType.BODY);
		dateFieldExtractor.setValue("/test/o/text()");
		dateField.setExtractor(dateFieldExtractor);

		FieldExtractor keyField = new FieldExtractor();
		keyField.setName("key");
		Extractor keyFieldExtractor = new Extractor();
		keyFieldExtractor.setType("xpath");
		keyFieldExtractor.setSource(SourceType.BODY);
		keyFieldExtractor.setValue("//t/text()");
		keyField.setExtractor(keyFieldExtractor);

		extractor.setFields(Arrays.asList(dateField, keyField));

		Extractor multiple = new Extractor();
		multiple.setType("xpath");
		multiple.setSource(SourceType.BODY);
		multiple.setValue("/html/body/test");

		extractor.setMultiple(multiple);

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
		Document data1 = new Document();
		data1.put("date", Arrays.asList("value1"));
		data1.put("key", Arrays.asList("key1"));

		Document data2 = new Document();
		data2.put("date", Arrays.asList("value2"));
		data2.put("key", Arrays.asList("key2"));

		Document data3 = new Document();
		data3.put("date", Arrays.asList("value3"));

		Mockito.verify(dataStore).save(Arrays.asList(data1, data2, data3));
	}

}
