package io.mandrel.http.proxy;

import io.mandrel.common.data.Spider;
import lombok.Data;

import com.ning.http.client.ProxyServer;

@Data
public class NoProxyProxyServersSource implements ProxyServersSource {

	private static final long serialVersionUID = 3055822574410617130L;

	public NoProxyProxyServersSource() {
	}

	public ProxyServer findProxy(Spider spider) {
		return null;
	}
}
