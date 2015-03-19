package io.mandrel.data.content.selector;

import io.mandrel.data.content.SourceType;
import io.mandrel.http.WebPage;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;

public abstract class HeaderSelector implements Selector {

	public abstract Instance init(WebPage webpage, FluentCaseInsensitiveStringsMap headers);

	@Override
	public final SourceType getSource() {
		return SourceType.HEADERS;
	}
}
