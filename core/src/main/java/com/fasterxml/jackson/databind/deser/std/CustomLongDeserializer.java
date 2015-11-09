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
