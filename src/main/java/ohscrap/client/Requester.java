package ohscrap.client;

import groovyjarjarantlr.StringUtils;
import ohscrap.client.dns.DnsCache;
import ohscrap.client.dns.InternalDnsCache;
import ohscrap.client.proxy.InternalProxyServersSource;
import ohscrap.client.proxy.ProxyServersSource;
import ohscrap.client.ua.FixedUserAgentProvisionner;
import ohscrap.client.ua.UserAgentProvisionner;
import ohscrap.common.Settings;
import ohscrap.common.Spider;

import com.google.common.base.Strings;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;
import com.ning.http.client.extra.ThrottleRequestFilter;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;

public class Requester {

	private final AsyncHttpClient client;

	private final UserAgentProvisionner userAgentProvisionner;

	private final DnsCache dnsCache;

	private final ProxyServersSource proxyServersSource;

	private final Settings settings;

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
		this.userAgentProvisionner = new FixedUserAgentProvisionner("OhScrap");
		this.dnsCache = new InternalDnsCache();
		this.proxyServersSource = new InternalProxyServersSource(settings);
	}

	public void get(String url, Spider spider) {
		BoundRequestBuilder request = client.prepareGet(dnsCache
				.optimizeUrl(url));

		request.setRequestTimeout(spider.getRequestTimeOut());
		request.setFollowRedirects(true);
		request.setHeaders(spider.getHeaders());
		// request.setCookies(cookies)
		// request.setQueryParams(params)
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

				if (statusCode >= 300) {
					// TODO Skip
				}

				return super.onStatusReceived(status);
			}

			@Override
			public STATE onHeadersReceived(HttpResponseHeaders headers)
					throws Exception {

				String header = headers.getHeaders().getFirstValue("");

				return super.onHeadersReceived(headers);
			}

			@Override
			public Response onCompleted(Response response) throws Exception {
				return null;
			}
		});
	}
}
