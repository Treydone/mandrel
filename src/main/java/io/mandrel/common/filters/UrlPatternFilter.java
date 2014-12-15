package io.mandrel.common.filters;

import java.util.regex.Pattern;

import io.mandrel.common.WebPage;

public class UrlPatternFilter implements WebPageFilter {

	private final Pattern pattern;

	public UrlPatternFilter(String pattern) {
		this.pattern = Pattern.compile(pattern);
	}

	public boolean isValid(WebPage webPage) {
		return pattern.matcher(webPage.getUrl().toString()).matches();
	}
}
