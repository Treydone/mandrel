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