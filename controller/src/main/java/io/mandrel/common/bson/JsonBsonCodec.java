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
package io.mandrel.common.bson;

import java.io.IOException;

import lombok.SneakyThrows;

import org.bson.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JsonBsonCodec {

	@SneakyThrows(JsonProcessingException.class)
	public static Document toBson(ObjectMapper mapper, Object value) {
		String json = mapper.writeValueAsString(value);
		json = json.replaceAll("\\.", "\\+\\+");
		return Document.parse(json);
	}

	@SneakyThrows(IOException.class)
	public static <T> T fromBson(ObjectMapper mapper, Document doc, Class<T> clazz) {
		String json = doc.toJson();
		json = json.replaceAll("\\+\\+", "\\.");
		return mapper.readValue(json, clazz);
	}
}
