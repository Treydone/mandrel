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

import io.mandrel.blob.Blob;
import io.mandrel.data.content.FieldExtractor;
import io.mandrel.document.Document;

import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Data
@Slf4j
public class JsonExporter implements DocumentExporter, RawExporter {

	private static final long serialVersionUID = -410119107553820985L;

	private transient ObjectMapper mapper;

	private transient Writer writer;

	@Override
	public void init(Writer writer) {
		this.writer = writer;
		mapper = new ObjectMapper();
	}

	@Override
	public void close() throws Exception {
		writer.flush();
		writer.close();
	}

	@Override
	public void export(Collection<Document> documents, List<FieldExtractor> fields) {

		ArrayNode arrayNode = mapper.createArrayNode();

		List<String> headers = fields.stream().map(field -> field.getName()).collect(Collectors.toList());

		documents.forEach(doc -> {
			ObjectNode rootNode = arrayNode.objectNode();
			for (String header : headers) {
				List<? extends Object> values = doc.get(header);
				if (!CollectionUtils.isEmpty(values)) {
					rootNode.putPOJO(header, values);
				}
			}
		});

		try {
			mapper.writeValue(writer, arrayNode);
		} catch (Exception e) {
			log.debug("Can not write docs", e);
		}
	}

	@Override
	public String contentType() {
		return "application/json; charset=utf-8";
	}

	@Override
	public void export(Collection<Blob> blobs) {

	}

	@Override
	public String name() {
		return "json";
	}
}
