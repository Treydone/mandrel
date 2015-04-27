package io.mandrel.data.content.selector;

import io.mandrel.data.content.SourceType;
import io.mandrel.http.WebPage;

import java.io.InputStream;

public abstract class BodySelector<X> implements Selector<X> {

	public abstract Instance<X> init(WebPage webpage, InputStream data, boolean isSegment);

	@Override
	public final SourceType getSource() {
		return SourceType.BODY;
	}

}
