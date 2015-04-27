package io.mandrel.data.content.selector;

import io.mandrel.http.Cookie;
import io.mandrel.http.WebPage;

import java.util.List;

public class SimpleCookieSelector extends CookieSelector<String> {

	public Instance<String> init(WebPage webpage, List<Cookie> cookies) {
		return new Instance<String>() {
			@Override
			public <T> List<T> select(String value, DataConverter<String, T> converter) {
				cookies.stream().filter(cookie -> cookie.getName().equals(value)).map(cookie -> converter.convert(cookie.getValue()));
				return null;
			}
		};
	}

	@Override
	public String getName() {
		return "cookie";
	}
}
