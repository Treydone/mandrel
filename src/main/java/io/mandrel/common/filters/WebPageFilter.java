package io.mandrel.common.filters;

import io.mandrel.common.WebPage;

public interface WebPageFilter {

	boolean isValid(WebPage webPage);
}
