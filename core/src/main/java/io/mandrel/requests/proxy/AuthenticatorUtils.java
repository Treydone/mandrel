package io.mandrel.requests.proxy;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public final class AuthenticatorUtils {

	public static String computeBasicAuthentication(Realm realm) {
		return computeBasicAuthentication(realm.getPrincipal(), realm.getPassword(), realm.getCharset());
	}

	public static String computeBasicAuthentication(ProxyServer proxyServer) {
		return computeBasicAuthentication(proxyServer.getPrincipal(), proxyServer.getPassword(), proxyServer.getCharset());
	}

	private static String computeBasicAuthentication(String principal, String password, Charset charset) {
		String s = principal + ":" + password;
		return "Basic " + Base64.encode(s.getBytes(charset));
	}

	public static String computeRealmURI(Realm realm) {
		return computeRealmURI(realm.getUri(), realm.isUseAbsoluteURI(), realm.isOmitQuery());
	}

	public static String computeRealmURI(URI uri, boolean useAbsoluteURI, boolean omitQuery) {
		if (useAbsoluteURI) {
			return omitQuery && StringUtils.isNotEmpty(uri.getQuery()) ? UriComponentsBuilder.fromUri(uri).query(null).build().toUri().toString() : uri
					.toString();
		} else {
			String path = StringUtils.isNotEmpty(uri.getPath()) ? uri.getPath() : "/";
			return omitQuery || !StringUtils.isNotEmpty(uri.getQuery()) ? path : path + "?" + uri.getQuery();
		}
	}

	public static String computeDigestAuthentication(Realm realm) {

		StringBuilder builder = new StringBuilder().append("Digest ");
		append(builder, "username", realm.getPrincipal(), true);
		append(builder, "realm", realm.getRealmName(), true);
		append(builder, "nonce", realm.getNonce(), true);
		append(builder, "uri", computeRealmURI(realm), true);
		if (StringUtils.isNotEmpty(realm.getAlgorithm()))
			append(builder, "algorithm", realm.getAlgorithm(), false);

		append(builder, "response", realm.getResponse(), true);

		if (realm.getOpaque() != null)
			append(builder, "opaque", realm.getOpaque(), true);

		if (realm.getQop() != null) {
			append(builder, "qop", realm.getQop(), false);
			// nc and cnonce only sent if server sent qop
			append(builder, "nc", realm.getNc(), false);
			append(builder, "cnonce", realm.getCnonce(), true);
		}
		builder.setLength(builder.length() - 2); // remove tailing ", "

		// FIXME isn't there a more efficient way?
		return new String(ISO_8859_1.encode(CharBuffer.wrap(builder)).toString());
	}

	private static StringBuilder append(StringBuilder builder, String name, String value, boolean quoted) {
		builder.append(name).append('=');
		if (quoted)
			builder.append('"').append(value).append('"');
		else
			builder.append(value);

		return builder.append(", ");
	}
}