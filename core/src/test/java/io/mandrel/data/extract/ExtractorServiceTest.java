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
import io.mandrel.data.Link;
import io.mandrel.data.content.Extractor;
import io.mandrel.data.content.FieldExtractor;
import io.mandrel.data.content.MetadataExtractor;
import io.mandrel.data.content.OutlinkExtractor;
import io.mandrel.data.content.SourceType;
import io.mandrel.document.Document;
import io.mandrel.document.DocumentStore;
import io.mandrel.document.DocumentStores;
import io.mandrel.io.Payloads;
import io.mandrel.script.ScriptingService;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.assertj.core.api.Assertions;
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
	private DocumentStore documentStore;

	private ExtractorService extractorService;

	@Before
	public void init() {
		extractorService = new ExtractorService(scriptingService);
	}

	@Test(expected = NullPointerException.class)
	public void no_matching_pattern() throws MalformedURLException {

		// Arrange
		Blob blob = new Blob(new BlobMetadata().uri(URI.create("http://localhost")));
		MetadataExtractor extractor = new MetadataExtractor();

		// Actions
		extractorService.extractThenFormatThenStore(0, new HashMap<>(), blob, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field() throws MalformedURLException {

		// Arrange
		Blob blob = new Blob(new BlobMetadata().uri(URI.create("http://localhost")));
		MetadataExtractor extractor = new MetadataExtractor();

		// Actions
		extractorService.extractThenFormatThenStore(0, new HashMap<>(), blob, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_DocumentStore() throws MalformedURLException {

		// Arrange
		Blob blob = new Blob(new BlobMetadata().uri(URI.create("http://localhost")));
		MetadataExtractor extractor = new MetadataExtractor();

		FieldExtractor field = new FieldExtractor();
		field.setName("date");
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractThenFormatThenStore(0, new HashMap<>(), blob, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field_extractor() throws MalformedURLException {

		// Arrange
		Blob blob = new Blob(new BlobMetadata().uri(URI.create("http://localhost")));
		MetadataExtractor extractor = new MetadataExtractor();

		DocumentStores.add(0, extractor.getName(), documentStore);
		FieldExtractor field = new FieldExtractor();
		field.setName("date");
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractThenFormatThenStore(0, new HashMap<>(), blob, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field_extractor_type() throws MalformedURLException {

		// Arrange
		Blob blob = new Blob(new BlobMetadata().uri(URI.create("http://localhost")));
		MetadataExtractor extractor = new MetadataExtractor();

		DocumentStores.add(0, extractor.getName(), documentStore);
		FieldExtractor field = new FieldExtractor();
		field.setName("date");
		Extractor fieldExtractor = new Extractor();
		fieldExtractor.setValue("");
		field.setExtractor(fieldExtractor);
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractThenFormatThenStore(0, new HashMap<>(), blob, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field_extractor_value() throws MalformedURLException {

		// Arrange
		Blob blob = new Blob(new BlobMetadata().uri(URI.create("http://localhost")));
		MetadataExtractor extractor = new MetadataExtractor();

		DocumentStores.add(0, extractor.getName(), documentStore);
		FieldExtractor field = new FieldExtractor();
		field.setName("date");
		Extractor fieldExtractor = new Extractor();
		fieldExtractor.setType("xpath");
		field.setExtractor(fieldExtractor);
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractThenFormatThenStore(0, new HashMap<>(), blob, extractor);

		// Asserts
	}

	@Test
	public void simple() throws MalformedURLException {

		// Arrange
		byte[] stream = "<html><test><o>value1</o><t>key1</t></test><test><o>value2</o></test></html>".getBytes();

		Blob blob = new Blob(new BlobMetadata().uri(URI.create("http://localhost"))).payload(Payloads.newByteArrayPayload(stream));
		MetadataExtractor extractor = new MetadataExtractor();

		DocumentStores.add(0, extractor.getName(), documentStore);
		FieldExtractor field = new FieldExtractor();
		field.setName("date");
		Extractor fieldExtractor = new Extractor();
		fieldExtractor.setType("xpath");
		fieldExtractor.setSource(SourceType.BODY);
		fieldExtractor.setValue("//test/o/text()");
		field.setExtractor(fieldExtractor);
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractThenFormatThenStore(0, new HashMap<>(), blob, extractor);

		// Asserts
		Document data = new Document();
		data.put("date", Arrays.asList("value1", "value2"));
		Mockito.verify(documentStore).save(Arrays.asList(data));
	}

	@Test
	public void simple_with_mutiple_extractors() throws MalformedURLException {

		// Arrange
		byte[] stream = "<html><test><o>value1</o><t>key1</t></test><test><o>value2</o></test></html>".getBytes();

		Blob blob = new Blob(new BlobMetadata().uri(URI.create("http://localhost"))).payload(Payloads.newByteArrayPayload(stream));
		MetadataExtractor extractor = new MetadataExtractor();

		DocumentStores.add(0, extractor.getName(), documentStore);

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
		extractorService.extractThenFormatThenStore(0, new HashMap<>(), blob, extractor);

		// Asserts
		Document data = new Document();
		data.put("date", Arrays.asList("value1", "value2"));
		data.put("key", Arrays.asList("key1"));
		Mockito.verify(documentStore).save(Arrays.asList(data));
	}

	@Test
	public void multiple() throws MalformedURLException {

		// Arrange
		byte[] stream = "<html><body><test><o>value1</o><t>key1</t></test><test><o>value2</o><t>key2</t></test><test><o>value3</o></test></body></html>"
				.getBytes();

		Blob blob = new Blob(new BlobMetadata().uri(URI.create("http://localhost"))).payload(Payloads.newByteArrayPayload(stream));
		MetadataExtractor extractor = new MetadataExtractor();
		DocumentStores.add(0, extractor.getName(), documentStore);

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
		extractorService.extractThenFormatThenStore(0, new HashMap<>(), blob, extractor);

		// Asserts
		Document data1 = new Document();
		data1.put("date", Arrays.asList("value1"));
		data1.put("key", Arrays.asList("key1"));

		Document data2 = new Document();
		data2.put("date", Arrays.asList("value2"));
		data2.put("key", Arrays.asList("key2"));

		Document data3 = new Document();
		data3.put("date", Arrays.asList("value3"));

		Mockito.verify(documentStore).save(Arrays.asList(data1, data2, data3));
	}

	@Test
	public void outlinks() throws MalformedURLException {

		// Arrange
		byte[] stream = "<html><body><a href='http://test.com/pouet'>Absolute</a><a href='/pouet'>Relative</a></body></html>".getBytes();

		Blob blob = new Blob(new BlobMetadata().uri(URI.create("http://localhost"))).payload(Payloads.newByteArrayPayload(stream));
		OutlinkExtractor extractor = new OutlinkExtractor("_default");

		// Actions
		Set<Link> links = extractorService.extractOutlinks(new HashMap<>(), blob, extractor);

		// Asserts
		Assertions.assertThat(links).containsExactly(new Link().text("Relative").uri(URI.create("http://localhost/pouet")),
				new Link().text("Absolute").uri(URI.create("http://test.com/pouet")));
	}
}
