package io.mandrel.requester.proxy;

import io.mandrel.service.spider.Spider;

import com.ning.http.client.ProxyServer;

public interface ProxyServersSource {

	ProxyServer findProxy(Spider spider);
}
