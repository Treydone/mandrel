package io.mandrel.data.content.selector;

import io.mandrel.http.Cookie;
import io.mandrel.http.WebPage;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleCookieSelector extends CookieSelector<String> {

	public Instance<String> init(WebPage webpage, List<Cookie> cookies) {
		return new Instance<String>() {
			@Override
			public <T> List<T> select(String value, DataConverter<String, T> converter) {
				return cookies.stream().filter(cookie -> cookie.getName().equals(value)).map(cookie -> converter.convert(cookie.getValue()))
						.collect(Collectors.toList());
			}
		};
	}

	@Override
	public String getName() {
		return "cookie";
	}
}
