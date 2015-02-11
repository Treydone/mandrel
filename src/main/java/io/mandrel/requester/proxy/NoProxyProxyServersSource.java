package io.mandrel.requester.proxy;

import io.mandrel.service.spider.Spider;
import lombok.Data;

import com.ning.http.client.ProxyServer;

@Data
public class NoProxyProxyServersSource implements ProxyServersSource {

	public NoProxyProxyServersSource() {
	}

	public ProxyServer findProxy(Spider spider) {
		return null;
	}
}
