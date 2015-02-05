package io.mandrel.requester;

import io.mandrel.common.WebPage;
import io.mandrel.common.settings.Settings;
import io.mandrel.requester.dns.DnsCache;
import io.mandrel.requester.dns.InternalDnsCache;
import io.mandrel.requester.proxy.InternalProxyServersSource;
import io.mandrel.requester.proxy.ProxyServersSource;
import io.mandrel.requester.ua.FixedUserAgentProvisionner;
import io.mandrel.requester.ua.UserAgentProvisionner;
import io.mandrel.service.spider.Spider;

import java.net.URL;

import javax.annotation.Resource;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Strings;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;
import com.ning.http.client.extra.ThrottleRequestFilter;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;

@Resource
@Slf4j
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
			public Response onCompleted(Response response) throws Exception {
				WebPage webPage;
				try {
					webPage = new WebPage(new URL(url), response
							.getStatusCode(), response.getStatusText(),
							response.getHeaders(), response.getCookies(),
							response.getResponseBodyAsStream());
					callback.on(webPage);
				} catch (Exception e) {
					log.debug("Can not construct web page", e);
				}
				return response;
			}
		});
	}

	interface Callback {

		void on(WebPage webapge);
	}
}
