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
package io.mandrel.data.export;

import io.mandrel.data.content.FieldExtractor;
import io.mandrel.document.Document;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DelimiterSeparatedValuesExporterTest {

	@Test
	public void export_documents_default() throws Exception {

		// Arrange
		DelimiterSeparatedValuesExporter exporter = new DelimiterSeparatedValuesExporter();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(out);

		List<Document> documents = new ArrayList<>();

		Document document1 = new Document();
		document1.put("key1", Arrays.asList("value1"));
		document1.put("key2", Arrays.asList("value2", "value2b"));
		documents.add(document1);

		Document document2 = new Document();
		document2.put("key1", Arrays.asList("value11111"));
		document2.put("key2", Arrays.asList("value22222", "value22222b"));
		documents.add(document2);

		Document document3 = new Document();
		document3.put("key2", Arrays.asList("utf-8 *$!/èé&à@", "+\""));
		documents.add(document3);

		List<FieldExtractor> fields = new ArrayList<>();
		fields.add(new FieldExtractor().setName("key1"));
		fields.add(new FieldExtractor().setName("key2"));

		// Actions
		exporter.init(writer);
		exporter.export(documents, fields);
		exporter.close();

		// Asserts
		String result = new String(out.toByteArray());
		Assertions.assertThat(result).contains("key1,key2").contains("value1,value2|value2b").contains("value11111,value22222|value22222b")
				.contains(",\"utf-8 *$!/èé&à@|+\"");
	}
}
