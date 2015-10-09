package io.mandrel.io.payload;

import static com.google.common.base.Preconditions.checkNotNull;
import io.mandrel.io.ContentMetadata;
import io.mandrel.io.Payload;

public abstract class BasePayload<V> implements Payload {
	protected final V rawContent;
	protected ContentMetadata contentMetadata;

	protected BasePayload(V content, ContentMetadata contentMetadata) {
		this.rawContent = checkNotNull(content, "content");
		this.contentMetadata = checkNotNull(contentMetadata, "contentMetadata");
	}

	protected BasePayload(V content) {
		this.rawContent = checkNotNull(content, "content");
		this.contentMetadata = new ContentMetadata();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V rawContent() {
		return rawContent;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public ContentMetadata contentMetadata() {
		return contentMetadata;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void contentMetadata(ContentMetadata in) {
		this.contentMetadata = in;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rawContent == null) ? 0 : rawContent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Payload))
			return false;
		Payload other = (Payload) obj;
		if (rawContent == null) {
			if (other.rawContent() != null)
				return false;
		} else if (!rawContent.equals(other.rawContent()))
			return false;
		return true;
	}

	/**
	 * By default we are repeatable.
	 */
	@Override
	public boolean isRepeatable() {
		return true;
	}

	/**
	 * By default there are no resources to release.
	 */
	@Override
	public void release() {
	}

	/**
	 * Delegates to release()
	 */
	@Override
	public void close() {
		release();
	}
}