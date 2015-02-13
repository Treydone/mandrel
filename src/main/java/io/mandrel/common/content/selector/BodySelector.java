package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.SourceType;

import java.io.InputStream;

public abstract class BodySelector implements Selector {

	public abstract Instance init(WebPage webpage, InputStream data);

	@Override
	public final SourceType getSource() {
		return SourceType.BODY;
	}
}
