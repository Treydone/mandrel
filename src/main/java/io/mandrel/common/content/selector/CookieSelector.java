package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.SourceType;

import java.util.List;

import com.ning.http.client.cookie.Cookie;

public abstract class CookieSelector implements Selector {

	public abstract Instance init(WebPage webpage, List<Cookie> cookies);

	@Override
	public final SourceType getSource() {
		return SourceType.COOKIE;
	}
}
