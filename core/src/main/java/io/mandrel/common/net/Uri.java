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
package io.mandrel.common.net;

import java.net.URI;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.google.common.annotations.VisibleForTesting;

@Accessors(chain = true)
@ThriftStruct
public class Uri {

	private static final String INTERNAL_SCHEME = "mandrel";

	public static String internalScheme() {
		return INTERNAL_SCHEME;
	}

	public static Uri internal(String host, int port) {
		return new Uri(internalScheme(), null, host, port, null, null);
	}

	public static Uri create(String originalUri) {
		return create(null, originalUri);
	}

	public static Uri create(URI originalUri) {
		return new Uri(originalUri.getScheme(), originalUri.getUserInfo(), originalUri.getHost(), originalUri.getPort(), originalUri.getPath(),
				originalUri.getQuery());
	}

	public static Uri create(Uri context, final String originalUri) {
		UriParser parser = new UriParser();
		parser.parse(context, originalUri);

		return new Uri(parser.scheme,//
				parser.userInfo,//
				parser.host,//
				parser.port,//
				parser.path,//
				parser.query);
	}

	@VisibleForTesting
	public Uri() {

	}

	@Getter(onMethod = @__(@ThriftField(1)))
	@Setter(onMethod = @__(@ThriftField))
	private String scheme;
	@Getter(onMethod = @__(@ThriftField(2)))
	@Setter(onMethod = @__(@ThriftField))
	private String query;
	@Getter(onMethod = @__(@ThriftField(3)))
	@Setter(onMethod = @__(@ThriftField))
	private String userInfo;
	@Getter(onMethod = @__(@ThriftField(4)))
	@Setter(onMethod = @__(@ThriftField))
	private String host;
	@Getter(onMethod = @__(@ThriftField(5)))
	@Setter(onMethod = @__(@ThriftField))
	private int port;
	@Getter(onMethod = @__(@ThriftField(6)))
	@Setter(onMethod = @__(@ThriftField))
	private String path;

	public Uri(String scheme,//
			String userInfo,//
			String host,//
			int port,//
			String path,//
			String query) {

		if (scheme == null)
			throw new NullPointerException("scheme");
		if (host == null)
			throw new NullPointerException("host");

		this.scheme = scheme;
		this.userInfo = userInfo;
		this.host = host;
		this.port = port;
		this.path = path;
		this.query = query;
	}

	public URI toURI() {
		return URI.create(toUri());
	}

	public String toUri() {
		StringBuilder sb = new StringBuilder();
		sb.append(scheme).append("://");
		if (userInfo != null)
			sb.append(userInfo).append('@');
		sb.append(host);
		if (port != -1)
			sb.append(':').append(port);
		if (path != null)
			sb.append(path);
		if (query != null)
			sb.append('?').append(query);

		return sb.toString();
	}

	@Override
	public String toString() {
		// for now, but might change
		return toUri();
	}

	final static class UriParser {

		public String scheme;
		public String host;
		public int port = -1;
		public String query;
		public String authority;
		public String path;
		public String userInfo;

		private int start, end = 0;
		private String urlWithoutQuery;

		private void trimRight(String originalUri) {
			end = originalUri.length();
			while (end > 0 && originalUri.charAt(end - 1) <= ' ')
				end--;
		}

		private void trimLeft(String originalUri) {
			while (start < end && originalUri.charAt(start) <= ' ')
				start++;

			if (originalUri.regionMatches(true, start, "url:", 0, 4))
				start += 4;
		}

		private boolean isFragmentOnly(String originalUri) {
			return start < originalUri.length() && originalUri.charAt(start) == '#';
		}

		private boolean isValidProtocolChar(char c) {
			return Character.isLetterOrDigit(c) && c != '.' && c != '+' && c != '-';
		}

		private boolean isValidProtocolChars(String protocol) {
			for (int i = 1; i < protocol.length(); i++) {
				if (!isValidProtocolChar(protocol.charAt(i)))
					return false;
			}
			return true;
		}

		private boolean isValidProtocol(String protocol) {
			return protocol.length() > 0 && Character.isLetter(protocol.charAt(0)) && isValidProtocolChars(protocol);
		}

		private void computeInitialScheme(String originalUri) {
			for (int i = start; i < end; i++) {
				char c = originalUri.charAt(i);
				if (c == ':') {
					String s = originalUri.substring(start, i);
					if (isValidProtocol(s)) {
						scheme = s.toLowerCase();
						start = i + 1;
					}
					break;
				} else if (c == '/')
					break;
			}
		}

		private boolean overrideWithContext(Uri context, String originalUri) {

			boolean isRelative = false;

			// only use context if the schemes match
			if (context != null && (scheme == null || scheme.equalsIgnoreCase(context.getScheme()))) {

				// see RFC2396 5.2.3
				String contextPath = context.getPath();
				if (isNotEmpty(contextPath) && contextPath.charAt(0) == '/')
					scheme = null;

				if (scheme == null) {
					scheme = context.getScheme();
					userInfo = context.getUserInfo();
					host = context.getHost();
					port = context.getPort();
					path = contextPath;
					isRelative = true;
				}
			}
			return isRelative;
		}

		private void computeFragment(String originalUri) {
			int charpPosition = originalUri.indexOf('#', start);
			if (charpPosition >= 0) {
				end = charpPosition;
			}
		}

		private void inheritContextQuery(Uri context, boolean isRelative) {
			// see RFC2396 5.2.2: query and fragment inheritance
			if (isRelative && start == end) {
				query = context.getQuery();
			}
		}

		private boolean splitUriAndQuery(String originalUri) {
			boolean queryOnly = false;
			urlWithoutQuery = originalUri;
			if (start < end) {
				int askPosition = originalUri.indexOf('?');
				queryOnly = askPosition == start;
				if (askPosition != -1 && askPosition < end) {
					query = originalUri.substring(askPosition + 1, end);
					if (end > askPosition)
						end = askPosition;
					urlWithoutQuery = originalUri.substring(0, askPosition);
				}
			}

			return queryOnly;
		}

		private boolean currentPositionStartsWith4Slashes() {
			return urlWithoutQuery.regionMatches(start, "////", 0, 4);
		}

		private boolean currentPositionStartsWith2Slashes() {
			return urlWithoutQuery.regionMatches(start, "//", 0, 2);
		}

		private void computeAuthority() {
			int authorityEndPosition = urlWithoutQuery.indexOf('/', start);
			if (authorityEndPosition < 0) {
				authorityEndPosition = urlWithoutQuery.indexOf('?', start);
				if (authorityEndPosition < 0)
					authorityEndPosition = end;
			}
			host = authority = urlWithoutQuery.substring(start, authorityEndPosition);
			start = authorityEndPosition;
		}

		private void computeUserInfo() {
			int atPosition = authority.indexOf('@');
			if (atPosition != -1) {
				userInfo = authority.substring(0, atPosition);
				host = authority.substring(atPosition + 1);
			} else
				userInfo = null;
		}

		private boolean isMaybeIPV6() {
			// If the host is surrounded by [ and ] then its an IPv6
			// literal address as specified in RFC2732
			return host.length() > 0 && host.charAt(0) == '[';
		}

		private void computeIPV6() {
			int positionAfterClosingSquareBrace = host.indexOf(']') + 1;
			if (positionAfterClosingSquareBrace > 1) {

				port = -1;

				if (host.length() > positionAfterClosingSquareBrace) {
					if (host.charAt(positionAfterClosingSquareBrace) == ':') {
						// see RFC2396: port can be null
						int portPosition = positionAfterClosingSquareBrace + 1;
						if (host.length() > portPosition) {
							port = Integer.parseInt(host.substring(portPosition));
						}
					} else
						throw new IllegalArgumentException("Invalid authority field: " + authority);
				}

				host = host.substring(0, positionAfterClosingSquareBrace);

			} else
				throw new IllegalArgumentException("Invalid authority field: " + authority);
		}

		private void computeRegularHostPort() {
			int colonPosition = host.indexOf(':');
			port = -1;
			if (colonPosition >= 0) {
				// see RFC2396: port can be null
				int portPosition = colonPosition + 1;
				if (host.length() > portPosition)
					port = Integer.parseInt(host.substring(portPosition));
				host = host.substring(0, colonPosition);
			}
		}

		// /./
		private void removeEmbeddedDot() {
			path = path.replace("/./", "");
		}

		// /../
		private void removeEmbedded2Dots() {
			int i = 0;
			while ((i = path.indexOf("/../", i)) >= 0) {
				if (i > 0) {
					end = path.lastIndexOf('/', i - 1);
					if (end >= 0 && path.indexOf("/../", end) != 0) {
						path = path.substring(0, end) + path.substring(i + 3);
						i = 0;
					}
				} else
					i = i + 3;
			}
		}

		private void removeTailing2Dots() {
			while (path.endsWith("/..")) {
				end = path.lastIndexOf('/', path.length() - 4);
				if (end >= 0)
					path = path.substring(0, end + 1);
				else
					break;
			}
		}

		private void removeStartingDot() {
			if (path.startsWith("./") && path.length() > 2)
				path = path.substring(2);
		}

		private void removeTrailingDot() {
			if (path.endsWith("/."))
				path = path.substring(0, path.length() - 1);
		}

		private void initRelativePath() {
			int lastSlashPosition = path.lastIndexOf('/');
			String pathEnd = urlWithoutQuery.substring(start, end);

			if (lastSlashPosition == -1)
				path = authority != null ? "/" + pathEnd : pathEnd;
			else
				path = path.substring(0, lastSlashPosition + 1) + pathEnd;
		}

		private void handlePathDots() {
			if (path.indexOf('.') != -1) {
				removeEmbeddedDot();
				removeEmbedded2Dots();
				removeTailing2Dots();
				removeStartingDot();
				removeTrailingDot();
			}
		}

		private void parseAuthority() {
			if (!currentPositionStartsWith4Slashes() && currentPositionStartsWith2Slashes()) {
				start += 2;

				computeAuthority();
				computeUserInfo();

				if (host != null) {
					if (isMaybeIPV6())
						computeIPV6();
					else
						computeRegularHostPort();
				}

				if (port < -1)
					throw new IllegalArgumentException("Invalid port number :" + port);

				// see RFC2396 5.2.4: ignore context path if authority is
				// defined
				if (isNotEmpty(authority))
					path = "";
			}
		}

		private void handleRelativePath() {
			initRelativePath();
			handlePathDots();
		}

		private void computeRegularPath() {
			if (urlWithoutQuery.charAt(start) == '/')
				path = urlWithoutQuery.substring(start, end);

			else if (isNotEmpty(path))
				handleRelativePath();

			else {
				String pathEnd = urlWithoutQuery.substring(start, end);
				path = authority != null ? "/" + pathEnd : pathEnd;
			}
		}

		private void computeQueryOnlyPath() {
			int lastSlashPosition = path.lastIndexOf('/');
			path = lastSlashPosition < 0 ? "/" : path.substring(0, lastSlashPosition) + "/";
		}

		private void computePath(boolean queryOnly) {
			// Parse the file path if any
			if (start < end)
				computeRegularPath();
			else if (queryOnly && path != null)
				computeQueryOnlyPath();
			else if (path == null)
				path = "";
		}

		public void parse(Uri context, final String originalUri) {

			if (originalUri == null)
				throw new NullPointerException("originalUri");

			boolean isRelative = false;

			trimRight(originalUri);
			trimLeft(originalUri);
			if (!isFragmentOnly(originalUri))
				computeInitialScheme(originalUri);
			overrideWithContext(context, originalUri);
			computeFragment(originalUri);
			inheritContextQuery(context, isRelative);

			boolean queryOnly = splitUriAndQuery(originalUri);
			parseAuthority();
			computePath(queryOnly);
		}

		private static boolean isNotEmpty(String string) {
			return string != null && string.length() > 0;
		}
	}
}
