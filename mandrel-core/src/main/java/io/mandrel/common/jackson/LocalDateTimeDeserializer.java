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
package io.mandrel.common.jackson;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

	@Override
	public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
			return LocalDateTime.ofInstant(Instant.ofEpochMilli(parser.getLongValue()), ZoneOffset.UTC);
		} else if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
			JsonNode node = parser.readValueAsTree();
			JsonNode number = node.get("$numberLong");
			if (number != null) {
				if (number.isNumber()) {
					return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.asLong()), ZoneOffset.UTC);
				} else if (number.isTextual()) {
					return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.valueOf(number.asText())), ZoneOffset.UTC);
				}
			}
		}
		throw context.wrongTokenException(parser, JsonToken.VALUE_NUMBER_INT, "Expected number or object with $numberLong property.");
	}
}