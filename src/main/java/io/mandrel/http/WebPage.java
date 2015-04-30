package io.mandrel.http;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;

@Data
public class WebPage {

	private final URL url;

	private final Metadata metadata;

	private final InputStream body;

	public WebPage(URL url, int statusCode, String statusText, FluentCaseInsensitiveStringsMap headers, List<Cookie> cookies, InputStream body) {
		super();
		this.url = url;
		this.metadata = new Metadata();
		metadata.setCookies(cookies);
		metadata.setHeaders(headers);
		metadata.setLastCrawlDate(LocalDateTime.now());
		metadata.setStatusCode(statusCode);
		metadata.setStatusText(statusText);
		metadata.setUrl(url);

		this.body = body;
	}
}
