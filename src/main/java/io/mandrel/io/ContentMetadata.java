package io.mandrel.io;

import java.util.Date;

import javax.annotation.Nullable;

import lombok.Data;
import lombok.experimental.Accessors;

import com.google.common.hash.HashCode;

@Data
@Accessors(fluent = true, chain = true)
public class ContentMetadata {

	/**
	 * 
	 * A standard MIME type describing the format of the contents. If none is
	 * provided, the default is binary/octet-stream.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17"
	 *      />
	 */
	@Nullable
	private String contentType = "binary/octet-stream";

	/**
	 * Returns the total size of the payload, or the chunk that's available.
	 * <p/>
	 * 
	 * @return the length in bytes that can be be obtained from
	 *         {@link #getInput()}
	 * @see com.google.common.net.HttpHeaders#CONTENT_LENGTH
	 */
	@Nullable
	private Long contentLength;

	@Nullable
	private HashCode contentMd5;

	/**
	 * Specifies presentational information for the object.
	 * 
	 * @see <a href=
	 *      "http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html?sec19.5.1."
	 *      />
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
	 * @see <a href=
	 *      "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html?sec14.11" />
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
	 * @see <a
	 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21"
	 *      />
	 */
	@Nullable
	private Date expires;
}
