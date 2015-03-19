package io.mandrel.data.content.selector;

import io.mandrel.data.content.SourceType;
import io.mandrel.http.WebPage;

public abstract class EmptySelector implements Selector {

	public abstract Instance init(WebPage webpage);

	@Override
	public final SourceType getSource() {
		return SourceType.EMPTY;
	}
}
