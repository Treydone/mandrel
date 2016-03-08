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
package io.mandrel.io;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;

import lombok.Data;
import lombok.experimental.Accessors;

import com.google.common.hash.HashCode;

@Data
@Accessors(fluent = true, chain = true)
public class ContentMetadata implements Serializable {

	private static final long serialVersionUID = -1102826235679727650L;

	/**
	 * 
	 * A standard MIME type describing the format of the contents. If none is
	 * provided, the default is binary/octet-stream.
	 * 
	 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17"/>
	 */
	@Nullable
	private String contentType = "binary/octet-stream";

	/**
	 * Returns the total size of the payload, or the chunk that's available.
	 * <p/>
	 * 
	 * @return the length in bytes that can be be obtained from {@link #getInput()}
	 * @see com.google.common.net.HttpHeaders#CONTENT_LENGTH
	 */
	@Nullable
	private Long contentLength;

	@Nullable
	private HashCode contentMd5;

	/**
	 * Specifies presentational information for the object.
	 * 
	 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html?sec19.5.1."/>
	 */
	@Nullable
	private String contentDisposition;

	/**
	 * Get Content Language of the payload
	 * <p/>
	 * Not all providers may support it
	 */
	@Nullable
	private String contentLanguage;

	/**
	 * Specifies what content encodings have been applied to the object and thus
	 * what decoding mechanisms must be applied in order to obtain the
	 * media-type referenced by the Content-Type header field.
	 * 
	 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html?sec14.11" />
	 */
	@Nullable
	private String contentEncoding;

	/**
	 * Gives the date/time after which the response is considered stale.
	 * 
	 * @throws IllegalStateException
	 *             If the Expires header is non-null, and not a valid RFC 1123
	 *             date
	 * 
	 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21" />
	 */
	@Nullable
	private Date expires;
}
