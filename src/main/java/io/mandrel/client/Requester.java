package io.mandrel.client;

import java.io.InputStream;

import io.mandrel.client.dns.DnsCache;
import io.mandrel.client.dns.InternalDnsCache;
import io.mandrel.client.proxy.InternalProxyServersSource;
import io.mandrel.client.proxy.ProxyServersSource;
import io.mandrel.client.ua.FixedUserAgentProvisionner;
import io.mandrel.client.ua.UserAgentProvisionner;
import io.mandrel.common.settings.Settings;
import io.mandrel.spider.Spider;

import javax.annotation.Resource;
import javax.inject.Inject;

import com.google.common.base.Strings;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHandler.STATE;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;
import com.ning.http.client.extra.ThrottleRequestFilter;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;

@Resource
public class Requester {

	private final AsyncHttpClient client;

	private final UserAgentProvisionner userAgentProvisionner;

	private final DnsCache dnsCache;

	private final ProxyServersSource proxyServersSource;

	private final Settings settings;

	@Inject
	public Requester(Settings settings) {
		this.settings = settings;

		NettyAsyncHttpProviderConfig nettyConfig = new NettyAsyncHttpProviderConfig();
		// nettyConfig.setBossExecutorService(taskExecutor);

		AsyncHttpClientConfig cf = new AsyncHttpClientConfig.Builder()
				// .setAllowPoolingConnections(true).setCompressionEnabled(true)
				// .setConnectionTimeoutInMs(10000)
				.setMaxRequestRetry(3)
				.setAsyncHttpClientProviderConfig(nettyConfig)
				// .setMaximumConnectionsPerHost(100)
				// .setMaximumConnectionsTotal(100)
				.addRequestFilter(new ThrottleRequestFilter(100))
				// .setExecutorService(taskExecutor)
				.build();

		this.client = new AsyncHttpClient(new NettyAsyncHttpProvider(cf), cf);
		this.userAgentProvisionner = new FixedUserAgentProvisionner("Mandrel");
		this.dnsCache = new InternalDnsCache();
		this.proxyServersSource = new InternalProxyServersSource(settings);
	}

	public void get(String url, Spider spider, Callback callback) {
		BoundRequestBuilder request = client.prepareGet(dnsCache
				.optimizeUrl(url));

		request.setRequestTimeout(spider.getRequestTimeOut());
		request.setFollowRedirects(true);
		request.setHeaders(spider.getHeaders());
		// request.setCookies(cookies)
		request.setQueryParams(spider.getParams());
		request.setProxyServer(proxyServersSource.findProxy(spider));

		String userAgent = userAgentProvisionner.get(url, spider);
		if (Strings.isNullOrEmpty(userAgent)) {
			request.addHeader("User-Agent", userAgent);
		}

		request.execute(new AsyncCompletionHandler<Response>() {

			@Override
			public STATE onStatusReceived(HttpResponseStatus status)
					throws Exception {
				int statusCode = status.getStatusCode();

				if (statusCode >= 400) {
					return STATE.ABORT;
				}

				return super.onStatusReceived(status);
			}

			@Override
			public STATE onHeadersReceived(HttpResponseHeaders headers)
					throws Exception {

				// TODO
				String header = headers.getHeaders().getFirstValue("");

				return super.onHeadersReceived(headers);
			}

			@Override
			public Response onCompleted(Response response) throws Exception {
				callback.on(response.getResponseBodyAsStream());
				return response;
			}
		});
	}

	interface Callback {

		void on(InputStream stream);
	}
}
