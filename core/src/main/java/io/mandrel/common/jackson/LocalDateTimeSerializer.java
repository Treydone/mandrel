package io.mandrel.common.jackson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

	@Override
	public void serialize(LocalDateTime value, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
		generator.writeNumber(value.toInstant(ZoneOffset.UTC).toEpochMilli());
	}
}
