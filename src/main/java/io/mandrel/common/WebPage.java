package io.mandrel.common;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import lombok.Data;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.cookie.Cookie;

@Data
public class WebPage {

	private final URL url;

	private final int statusCode;

	private final String statusText;

	private final FluentCaseInsensitiveStringsMap headers;

	private final List<Cookie> cookies;

	private final InputStream body;

	public WebPage(URL url, int statusCode, String statusText,
			FluentCaseInsensitiveStringsMap headers, List<Cookie> cookies,
			InputStream body) {
		super();
		this.url = url;
		this.statusCode = statusCode;
		this.statusText = statusText;
		this.headers = headers;
		this.cookies = cookies;
		this.body = body;
	}

}
