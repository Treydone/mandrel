package ohscrap.client.proxy;

import ohscrap.common.Spider;

import com.ning.http.client.ProxyServer;

public interface ProxyServersSource {

	ProxyServer findProxy(Spider spider);
}
