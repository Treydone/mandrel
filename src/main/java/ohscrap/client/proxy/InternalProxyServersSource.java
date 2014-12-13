package ohscrap.client.proxy;

import ohscrap.common.Settings;
import ohscrap.common.Spider;

import com.ning.http.client.ProxyServer;

public class InternalProxyServersSource implements ProxyServersSource {

	public InternalProxyServersSource(Settings settings) {
	}

	public ProxyServer findProxy(Spider spider) {
		return null;
	}
}
