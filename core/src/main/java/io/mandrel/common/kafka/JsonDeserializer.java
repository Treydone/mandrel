package io.mandrel.common.kafka;

import java.io.IOException;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

@RequiredArgsConstructor
public class JsonDeserializer implements Deserializer<Object> {

	private static final ObjectMapper mapper = new ObjectMapper();
	private Class<?> clazz;

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		try {
			clazz = Class.forName((String) configs.get("json.class"));
		} catch (ClassNotFoundException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public Object deserialize(String topic, byte[] data) {
		if (data == null) {
			return null;
		} else {
			try {
				return mapper.readValue(data, clazz);
			} catch (IOException e) {
				throw new SerializationException("Error when deserializing byte[] to class due to ", e);
			}
		}
	}

	@Override
	public void close() {
		// nothing to do
	}
}