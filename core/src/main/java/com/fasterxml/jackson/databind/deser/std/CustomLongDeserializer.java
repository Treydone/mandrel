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
package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.PrimitiveOrWrapperDeserializer;

@JacksonStdImpl
public final class CustomLongDeserializer extends PrimitiveOrWrapperDeserializer<Long> {
	private static final long serialVersionUID = 1L;

	public final static CustomLongDeserializer primitiveInstance = new CustomLongDeserializer(Long.TYPE, Long.valueOf(0L));
	public final static CustomLongDeserializer wrapperInstance = new CustomLongDeserializer(Long.class, null);

	public CustomLongDeserializer(Class<Long> cls, Long nvl) {
		super(cls, nvl);
	}

	// since 2.6, slightly faster lookups for this very common type
	@Override
	public boolean isCachable() {
		return true;
	}

	@Override
	public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		if (p.hasToken(JsonToken.START_OBJECT)) {
			JsonNode node = p.readValueAsTree();
			JsonNode number = node.get("$numberLong");
			if (number != null) {
				if (number.isNumber()) {
					return number.asLong();
				} else if (number.isTextual()) {
					return Long.valueOf(number.asText());
				}
			}
		} else if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
			return p.getLongValue();
		}
		return _parseLong(p, ctxt);
	}
}
