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
package io.mandrel.blob;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.mandrel.io.Payloads.newPayload;
import io.mandrel.io.Payload;

import java.io.File;
import java.io.InputStream;

import lombok.Data;
import lombok.experimental.Accessors;

import com.google.common.io.ByteSource;

@Data
@Accessors(chain = true)
public class Blob {

	private Payload payload;

	private final BlobMetadata metadata;

	public Blob setPayload(Payload data) {
		if (this.payload != null)
			payload.release();
		this.payload = checkNotNull(data, "data");
		return this;
	}

	public Blob payload(InputStream data) {
		return setPayload(newPayload(checkNotNull(data, "data")));
	}

	public Blob payload(byte[] data) {
		return setPayload(newPayload(checkNotNull(data, "data")));
	}

	public Blob payload(String data) {
		return setPayload(newPayload(checkNotNull(data, "data")));
	}

	public Blob payload(File data) {
		return setPayload(newPayload(checkNotNull(data, "data")));
	}

	public Blob payload(ByteSource data) {
		return setPayload(newPayload(checkNotNull(data, "data")));
	}
}
