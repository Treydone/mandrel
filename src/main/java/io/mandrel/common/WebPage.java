package io.mandrel.common;

import java.io.InputStream;
import java.net.URL;

import lombok.Data;

@Data
public class WebPage {

	private final URL url;

	private final InputStream dataStream;

	public WebPage(URL url, InputStream dataStream) {
		super();
		this.url = url;
		this.dataStream = dataStream;
	}
}
