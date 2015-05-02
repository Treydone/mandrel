package io.mandrel.data.content.selector;

import io.mandrel.data.content.SourceType;
import io.mandrel.http.WebPage;

import java.util.List;
import java.util.Map;

public abstract class HeaderSelector<X> implements Selector<X> {

	public abstract Instance<X> init(WebPage webpage, Map<String, List<String>> headers);

	@Override
	public final SourceType getSource() {
		return SourceType.HEADERS;
	}
}
