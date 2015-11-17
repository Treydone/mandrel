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