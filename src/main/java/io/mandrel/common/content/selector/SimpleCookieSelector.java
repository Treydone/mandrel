package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;

import java.util.List;

import com.ning.http.client.cookie.Cookie;

public class SimpleCookieSelector extends CookieSelector {

	public Instance init(WebPage webpage, List<Cookie> cookies) {
		return new Instance() {

			@Override
			public List<String> select(String value) {
				return null;
			}
		};
	}

	@Override
	public String getName() {
		return "cookie";
	}
}
