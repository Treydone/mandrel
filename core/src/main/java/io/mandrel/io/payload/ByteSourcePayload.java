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
