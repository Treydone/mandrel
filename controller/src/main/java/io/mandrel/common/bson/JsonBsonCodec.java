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
