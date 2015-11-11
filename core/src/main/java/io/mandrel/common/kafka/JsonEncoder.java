package io.mandrel.common.kafka;

import kafka.serializer.Encoder;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
public class JsonEncoder implements Encoder<Object> {

	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public byte[] toBytes(Object object) {
		try {
			return mapper.writeValueAsString(object).getBytes();
		} catch (JsonProcessingException e) {
			log.debug(String.format("Json processing failed for object: %s", object.getClass().getName()), e);
		}
		return "".getBytes();
	}
}