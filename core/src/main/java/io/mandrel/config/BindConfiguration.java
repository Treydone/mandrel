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
package io.mandrel.config;

import io.mandrel.common.jackson.LocalDateTimeDeserializer;
import io.mandrel.common.jackson.LocalDateTimeSerializer;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.CustomLongDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hppc.HppcModule;

@Configuration
public class BindConfiguration {

	@Bean
	public ObjectMapper mapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		configure(objectMapper);
		return objectMapper;
	}

	public static void configure(ObjectMapper objectMapper) {
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

		objectMapper.registerModules(
		// new JodaModule(),
		// new AfterburnerModule(),
		// new Jdk8Module(),
		// new JavaTimeModule(),
				new HppcModule());

		SimpleModule module = new SimpleModule();
		module.addDeserializer(Long.class, CustomLongDeserializer.wrapperInstance);
		module.addDeserializer(long.class, CustomLongDeserializer.primitiveInstance);

		module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
		module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
		objectMapper.registerModule(module);
	}
}
