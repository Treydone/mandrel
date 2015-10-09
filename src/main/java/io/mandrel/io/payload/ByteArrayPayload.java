package io.mandrel.io.payload;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.google.common.hash.HashCode;

public class ByteArrayPayload extends BasePayload<byte[]> {
	public ByteArrayPayload(byte[] content) {
		this(content, null);
	}

	public ByteArrayPayload(byte[] content, byte[] md5) {
		super(content);
		contentMetadata().contentLength(Long.valueOf(checkNotNull(content, "content").length));
		contentMetadata().contentMd5(md5 == null ? null : HashCode.fromBytes(md5));
		checkArgument(content.length >= 0, "length cannot me negative");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream openStream() {
		return new ByteArrayInputStream(rawContent);
	}
}
