package io.mandrel.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface Payload extends Closeable {

	/**
	 * Creates a new InputStream object of the payload.
	 */
	InputStream openStream() throws IOException;

	/**
	 * Payload in its original form.
	 */
	Object rawContent();

	/**
	 * Tells if the payload is capable of producing its data more than once.
	 */
	boolean isRepeatable();

	/**
	 * release resources used by this entity. This should be called when data is
	 * discarded.
	 */
	void release();

	ContentMetadata contentMetadata();

	void contentMetadata(ContentMetadata in);

}