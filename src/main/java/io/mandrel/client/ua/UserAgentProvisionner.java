package io.mandrel.client.ua;

import io.mandrel.spider.Spider;

public interface UserAgentProvisionner {

	String get(String url, Spider spider);
}
