/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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