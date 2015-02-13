package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.SourceType;

import java.net.URL;

public abstract class UrlSelector implements Selector {

	public abstract Instance init(WebPage webpage, URL url);

	@Override
	public final SourceType getSource() {
		return SourceType.URL;
	}
}
