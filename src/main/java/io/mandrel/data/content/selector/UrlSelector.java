package io.mandrel.data.content.selector;

import io.mandrel.data.content.SourceType;
import io.mandrel.http.WebPage;

import java.net.URL;

public abstract class UrlSelector implements Selector {

	public abstract Instance init(WebPage webpage, URL url);

	@Override
	public final SourceType getSource() {
		return SourceType.URL;
	}
}
