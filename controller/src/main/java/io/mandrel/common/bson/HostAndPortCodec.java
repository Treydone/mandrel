package io.mandrel.common.bson;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import com.google.common.net.HostAndPort;

public class HostAndPortCodec implements Codec<HostAndPort> {

	@Override
	public void encode(final BsonWriter writer, final HostAndPort value, final EncoderContext encoderContext) {
		writer.writeString(value.toString());
	}

	@Override
	public HostAndPort decode(final BsonReader reader, final DecoderContext decoderContext) {
		return HostAndPort.fromString(reader.readString());
	}

	@Override
	public Class<HostAndPort> getEncoderClass() {
		return HostAndPort.class;
	}
}