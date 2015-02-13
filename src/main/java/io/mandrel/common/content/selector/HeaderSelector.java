package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.SourceType;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;

public abstract class HeaderSelector implements Selector {

	public abstract Instance init(WebPage webpage, FluentCaseInsensitiveStringsMap headers);

	@Override
	public final SourceType getSource() {
		return SourceType.HEADERS;
	}
}
