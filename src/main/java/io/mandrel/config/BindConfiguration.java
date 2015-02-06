package io.mandrel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@Configuration
public class BindConfiguration {

	@Bean
	public ObjectMapper mapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		configure(objectMapper);
		return objectMapper;
	}

	protected void configure(ObjectMapper objectMapper) {
		// SerializationFeature for changing how JSON is written

		// to allow serialization of "empty" POJOs (no properties to serialize)
		// (without this setting, an exception is thrown in those cases)
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		// objectMapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
		// objectMapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
		// to write java.util.Date, Calendar as number (timestamp):
		objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// DeserializationFeature for changing how JSON is read as POJOs:

		// to prevent exception when encountering unknown property:
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// to allow coercion of JSON empty String ("") to null Object value:
		// objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

	}

	@Bean
	public JacksonJsonProvider jsonProvider() {
		return new JacksonJsonProvider(mapper());
	}

}
