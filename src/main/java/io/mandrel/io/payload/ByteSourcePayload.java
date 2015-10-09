package io.mandrel.io.payload;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.google.common.io.ByteSource;
import com.google.common.io.Closer;

/**
 * A repeatable, ByteSource-backed Payload.
 */
public class ByteSourcePayload extends BasePayload<ByteSource> {
	private final Closer closer = Closer.create();

	public ByteSourcePayload(ByteSource content) {
		super(content);
	}

	@Override
	public InputStream openStream() throws IOException {
		return closer.register(rawContent.openStream());
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	/**
	 * if we created the stream, then it is already consumed on close.
	 */
	@Override
	public void release() {
		IOUtils.closeQuietly(closer);
	}
}
