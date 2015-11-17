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
