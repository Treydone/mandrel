package io.mandrel.client.proxy;

import io.mandrel.common.settings.Settings;
import io.mandrel.service.spider.Spider;

import javax.annotation.Resource;
import javax.inject.Inject;

import com.ning.http.client.ProxyServer;

@Resource
public class InternalProxyServersSource implements ProxyServersSource {

	@Inject
	public InternalProxyServersSource(Settings settings) {
	}

	public ProxyServer findProxy(Spider spider) {
		return null;
	}
}
