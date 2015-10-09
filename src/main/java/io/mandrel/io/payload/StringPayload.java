package io.mandrel.io.payload;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.google.common.base.Charsets;

/**
 * This implementation converts the String to a byte array using UTF-8 encoding.
 * If you wish to use a different encoding, please use {@link ByteArrayPayload}.
 * 
 */
public class StringPayload extends BasePayload<String> {

	private final byte[] bytes;

	// it is possible to discover length by walking the string and updating
	// current length based on
	// character code. However, this is process intense, and assumes an encoding
	// type of UTF-8
	public StringPayload(String content) {
		super(content);
		this.bytes = content.getBytes(Charsets.UTF_8);
		contentMetadata().contentLength(Long.valueOf(bytes.length));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream openStream() {
		return new ByteArrayInputStream(bytes);
	}
}
