package io.mandrel.http;

import io.mandrel.common.data.Spider;

public interface Requester {

	void get(String url, Spider spider, SuccessCallback successCallback, FailureCallback failureCallback);

	@Deprecated
	WebPage getBlocking(String url, Spider spider) throws Exception;

	@Deprecated
	WebPage getBlocking(String url) throws Exception;

	@FunctionalInterface
	public static interface SuccessCallback {
		void on(WebPage webapge);
	}

	@FunctionalInterface
	public static interface FailureCallback {
		void on(Throwable t);
	}
}
