package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.SourceType;

public abstract class EmptySelector implements Selector {

	public abstract Instance init(WebPage webpage);

	@Override
	public final SourceType getSource() {
		return SourceType.EMPTY;
	}
}
