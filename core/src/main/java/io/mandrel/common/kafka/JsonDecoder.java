package io.mandrel.common.kafka;

import java.io.IOException;

import kafka.serializer.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
public class JsonDecoder<T> implements Decoder<T> {

	private static final ObjectMapper mapper = new ObjectMapper();

	private final Class<T> clazz;

	@Override
	public T fromBytes(byte[] data) {
		try {
			return mapper.readValue(data, clazz);
		} catch (IOException e) {
			log.debug(String.format("Json processing failed for object: %s", clazz), e);
		}
		return null;
	}
}
