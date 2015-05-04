package io.mandrel.http;

import io.mandrel.common.data.Spider;
import io.mandrel.common.settings.ClientSettings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.extra.ThrottleRequestFilter;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;

@Component
@Slf4j
public class Requester {

	private final AsyncHttpClient client;

	@Inject
	public Requester(ClientSettings settings) {
		NettyAsyncHttpProviderConfig nettyConfig = new NettyAsyncHttpProviderConfig();

		AsyncHttpClientConfig cf = new AsyncHttpClientConfig.Builder().setAllowPoolingConnections(true).setMaxRequestRetry(3).setCompressionEnforced(true)
				.setAllowPoolingConnections(true).setAllowPoolingSslConnections(true).setAsyncHttpClientProviderConfig(nettyConfig)
				.setMaxConnectionsPerHost(settings.getConnections().getHost()).setMaxConnections(settings.getConnections().getGlobal())
				.addRequestFilter(new ThrottleRequestFilter(100)).build();

		this.client = new AsyncHttpClient(new NettyAsyncHttpProvider(cf), cf);
	}

	public void get(String url, Spider spider, Callback callback) {
		log.trace("Requesting {}...", url);
		BoundRequestBuilder request = prepareRequest(url, spider);

		request.execute(new AsyncCompletionHandler<Response>() {
			@Override
			public Response onCompleted(Response response) throws Exception {
				try {
					WebPage webPage = extractWebPage(url, response);
					callback.on(webPage);
				} catch (Exception e) {
					log.debug("Can not construct web page", e);
					throw e;
				}
				return response;
			}
		});
	}

	// TODO use RxNetty with CompletableFuture
	@Deprecated
	public WebPage getBlocking(String url, Spider spider) throws Exception {
		BoundRequestBuilder request = prepareRequest(url, spider);
		Response response = request.execute().get(5000, TimeUnit.MILLISECONDS);
		return extractWebPage(url, response);
	}

	public BoundRequestBuilder prepareRequest(String url, Spider spider) {
		BoundRequestBuilder request = client.prepareGet(spider.getClient().getDnsCache().optimizeUrl(url));

		request.setRequestTimeout(spider.getClient().getRequestTimeOut());
		request.setFollowRedirects(spider.getClient().isFollowRedirects());
		request.setHeaders(spider.getClient().getHeaders());
		if (spider.getClient().getCookies() != null)
			request.setCookies(spider
					.getClient()
					.getCookies()
					.stream()
					.map(cookie -> new Cookie(cookie.getName(), cookie.getValue(), false, cookie.getDomain(), cookie.getPath(), cookie.getExpires(), cookie
							.getMaxAge(), cookie.isSecure(), cookie.isHttpOnly())).collect(Collectors.toList()));
		request.setQueryParams(spider.getClient().getParams());
		request.setProxyServer(spider.getClient().getProxyServersSource().findProxy(spider));

		String userAgent = spider.getClient().getUserAgentProvisionner().get(url, spider);
		if (Strings.isNullOrEmpty(userAgent)) {
			request.addHeader("User-Agent", userAgent);
		}
		return request;
	}

	public WebPage extractWebPage(String url, Response response) throws MalformedURLException, IOException {
		List<io.mandrel.http.Cookie> cookies = response
				.getCookies()
				.stream()
				.map(cookie -> new io.mandrel.http.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getExpires(),
						cookie.getMaxAge(), cookie.isSecure(), cookie.isHttpOnly())).collect(Collectors.toList());
		WebPage webPage = new WebPage(new URL(url), response.getStatusCode(), response.getStatusText(), new HashMap<>(response.getHeaders()), cookies,
				response.getResponseBodyAsBytes());
		return webPage;
	}

	@FunctionalInterface
	public static interface Callback {

		void on(WebPage webapge);
	}
}
