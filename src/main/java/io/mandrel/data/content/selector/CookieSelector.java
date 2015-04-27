package io.mandrel.data.content.selector;

import io.mandrel.data.content.SourceType;
import io.mandrel.http.Cookie;
import io.mandrel.http.WebPage;

import java.util.List;

public abstract class CookieSelector<X> implements Selector<X> {

	public abstract Instance<X> init(WebPage webpage, List<Cookie> cookies);

	@Override
	public final SourceType getSource() {
		return SourceType.COOKIE;
	}
}
