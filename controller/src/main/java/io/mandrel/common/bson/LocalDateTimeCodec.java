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
package io.mandrel.common.bson;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class LocalDateTimeCodec implements Codec<LocalDateTime> {

	@Override
	public void encode(final BsonWriter writer, final LocalDateTime value, final EncoderContext encoderContext) {
		writer.writeDateTime(value.toInstant(ZoneOffset.UTC).toEpochMilli());
	}

	@Override
	public LocalDateTime decode(final BsonReader reader, final DecoderContext decoderContext) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(reader.readDateTime()), ZoneOffset.UTC);
	}

	@Override
	public Class<LocalDateTime> getEncoderClass() {
		return LocalDateTime.class;
	}
}