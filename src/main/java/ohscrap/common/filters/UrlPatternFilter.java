package ohscrap.common.filters;

import ohscrap.common.WebPage;

public class UrlPatternFilter implements WebPageFilter {

	public boolean isValid(WebPage webPage) {
		// TODO
		webPage.getUrl();
		return true;
	}
}
