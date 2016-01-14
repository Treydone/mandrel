package io.mandrel.transport.thrift;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.facebook.swift.codec.internal.coercion.FromThrift;
import com.facebook.swift.codec.internal.coercion.ToThrift;

public class MandrelCoercions {
	
	@ToThrift
	public static long toThrift(LocalDateTime value) {
		return value.toInstant(ZoneOffset.UTC).toEpochMilli();
	}

	@FromThrift
	public static LocalDateTime fromThrift(long value) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC);
	}
}
