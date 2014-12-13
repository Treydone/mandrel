package ohscrap.common.filters;

import ohscrap.common.WebPage;

public interface WebPageFilter {

	boolean isValid(WebPage webPage);
}
