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