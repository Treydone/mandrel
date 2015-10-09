package io.mandrel.io.payload;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class InputStreamPayload extends BasePayload<InputStream> {

	public InputStreamPayload(InputStream content) {
		super(content);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream openStream() {
		return rawContent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRepeatable() {
		return false;
	}

	/**
	 * if we created the stream, then it is already consumed on close.
	 */
	@Override
	public void release() {
		IOUtils.closeQuietly(rawContent);
	}
}
