package io.mandrel.common.kafka;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer implements Serializer<Object> {

	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public byte[] serialize(String topic, Object data) {
		if (data == null) {
			return null;
		} else {
			try {
				return mapper.writeValueAsBytes(data);
			} catch (IOException e) {
				throw new SerializationException("Error when serializing class to byte[] due to ", e);
			}
		}
	}

	@Override
	public void close() {
		// nothing to do
	}
}