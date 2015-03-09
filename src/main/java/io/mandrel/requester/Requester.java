package io.mandrel.requester;

import io.mandrel.common.WebPage;
import io.mandrel.common.data.Spider;
import io.mandrel.common.settings.Settings;

import java.net.URL;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.extra.ThrottleRequestFilter;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;

@Component
@Slf4j
public class Requester {

	private final AsyncHttpClient client;

	private final Settings settings;

	@Inject
	public Requester(Settings settings) {
		this.settings = settings;

		NettyAsyncHttpProviderConfig nettyConfig = new NettyAsyncHttpProviderConfig();
		// nettyConfig.setBossExecutorService(taskExecutor);

		AsyncHttpClientConfig cf = new AsyncHttpClientConfig.Builder().setAllowPoolingConnections(true).setMaxRequestRetry(3)
				.setAsyncHttpClientProviderConfig(nettyConfig)
				// .setMaximumConnectionsPerHost(100)
				// .setMaximumConnectionsTotal(100)
				.addRequestFilter(new ThrottleRequestFilter(100))
				// .setExecutorService(taskExecutor)
				.build();

		this.client = new AsyncHttpClient(new NettyAsyncHttpProvider(cf), cf);
	}

	public void get(String url, Spider spider, Callback callback) {
		BoundRequestBuilder request = client.prepareGet(spider.getClient().getDnsCache().optimizeUrl(url));

		request.setRequestTimeout(spider.getClient().getRequestTimeOut());
		request.setFollowRedirects(spider.getClient().isFollowRedirects());
		request.setHeaders(spider.getClient().getHeaders());
		if (spider.getClient().getCookies() != null)
			request.setCookies(spider
					.getClient()
					.getCookies()
					.stream()
					.map(cookie -> new Cookie(cookie.getName(), cookie.getValue(), cookie.getRawValue(), cookie.getDomain(), cookie.getPath(), cookie
							.getExpires(), cookie.getMaxAge(), cookie.isSecure(), cookie.isHttpOnly())).collect(Collectors.toList()));
		request.setQueryParams(spider.getClient().getParams());
		request.setProxyServer(spider.getClient().getProxyServersSource().findProxy(spider));

		String userAgent = spider.getClient().getUserAgentProvisionner().get(url, spider);
		if (Strings.isNullOrEmpty(userAgent)) {
			request.addHeader("User-Agent", userAgent);
		}

		request.execute(new AsyncCompletionHandler<Response>() {

			@Override
			public STATE onStatusReceived(HttpResponseStatus status) throws Exception {
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
					webPage = new WebPage(new URL(url), response.getStatusCode(), response.getStatusText(), response.getHeaders(), response
							.getCookies(), response.getResponseBodyAsStream());
					callback.on(webPage);
				} catch (Exception e) {
					log.debug("Can not construct web page", e);
					throw e;
				}
				return response;
			}
		});
	}

	@FunctionalInterface
	public interface Callback {

		void on(WebPage webapge);
	}
}
