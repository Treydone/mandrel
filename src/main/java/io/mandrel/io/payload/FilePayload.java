package io.mandrel.io.payload;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FilePayload extends BasePayload<File> {

	public FilePayload(File content) {
		super(content);
		contentMetadata().contentLength(content.length());
		checkNotNull(content, "content");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream openStream() throws IOException {
		return new FileInputStream(rawContent);
	}
}
