package io.mandrel.client.proxy;

import io.mandrel.spider.Spider;

import com.ning.http.client.ProxyServer;

public interface ProxyServersSource {

	ProxyServer findProxy(Spider spider);
}
