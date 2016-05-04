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
package io.mandrel.transport.thrift;

import io.mandrel.common.data.JobDefinition;
import io.mandrel.config.BindConfiguration;
import io.mandrel.metrics.Timeserie;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.facebook.swift.codec.internal.coercion.FromThrift;
import com.facebook.swift.codec.internal.coercion.ToThrift;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

public class MandrelCoercions {

	private final static ObjectMapper objectMapper = new ObjectMapper();
	static {
		BindConfiguration.configure(objectMapper);
	}

	@ToThrift
	public static long toThrift(LocalDateTime value) {
		return value.toInstant(ZoneOffset.UTC).toEpochMilli();
	}

	@FromThrift
	public static LocalDateTime fromThrift(long value) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC);
	}

	@ToThrift
	public static String toThrift(HostAndPort value) {
		return value.toString();
	}

	@FromThrift
	public static HostAndPort fromThrift(String value) {
		return HostAndPort.fromString(value);
	}

	@ToThrift
	public static ByteBuffer toThrift(JobDefinition value) {
		try {
			return ByteBuffer.wrap(objectMapper.writeValueAsBytes(value));
		} catch (JsonProcessingException e) {
			throw Throwables.propagate(e);
		}
	}

	@FromThrift
	public static JobDefinition fromThrift(ByteBuffer buffer) {
		byte[] result = new byte[buffer.remaining()];
		buffer.duplicate().get(result);
		try {
			return objectMapper.readValue(result, JobDefinition.class);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@ToThrift
	public static ByteBuffer toThrift(Timeserie value) {
		try {
			return ByteBuffer.wrap(objectMapper.writeValueAsBytes(value));
		} catch (JsonProcessingException e) {
			throw Throwables.propagate(e);
		}
	}

	@FromThrift
	public static Timeserie fromThriftToTimeserie(ByteBuffer buffer) {
		byte[] result = new byte[buffer.remaining()];
		buffer.duplicate().get(result);
		try {
			return objectMapper.readValue(result, Timeserie.class);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

}
