///*
// * Licensed to Mandrel under one or more contributor
// * license agreements. See the NOTICE file distributed with
// * this work for additional information regarding copyright
// * ownership. Mandrel licenses this file to you under
// * the Apache License, Version 2.0 (the "License"); you may
// * not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package io.mandrel.http;
//
//import io.mandrel.common.data.Spider;
//import io.mandrel.common.settings.ClientSettings;
//import io.mandrel.http.dns.NameResolver;
//import io.mandrel.http.proxy.ProxyServer;
//
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.UnknownHostException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//import javax.inject.Inject;
//
//import lombok.extern.slf4j.Slf4j;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Component;
//
//import com.google.common.base.Strings;
//import com.ning.http.client.AsyncCompletionHandler;
//import com.ning.http.client.AsyncHttpClient;
//import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
//import com.ning.http.client.AsyncHttpClientConfig;
//import com.ning.http.client.ProxyServer.Protocol;
//import com.ning.http.client.Realm.AuthScheme;
//import com.ning.http.client.Response;
//import com.ning.http.client.cookie.Cookie;
//import com.ning.http.client.extra.ThrottleRequestFilter;
//import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
//import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;
//
//@Slf4j
//public class NingRequester extends Requester {
//
//	private final AsyncHttpClient client;
//
//	@Inject
//	public NingRequester(ClientSettings settings) {
//		NettyAsyncHttpProviderConfig nettyConfig = new NettyAsyncHttpProviderConfig();
//
//		ExecutorService threadPool = Executors.newFixedThreadPool(8, new ThreadFactory() {
//			public Thread newThread(Runnable r) {
//				Thread t = new Thread(r, "AsyncHttpClient-Callback");
//				t.setDaemon(true);
//				return t;
//			}
//		});
//
//		AsyncHttpClientConfig cf = new AsyncHttpClientConfig.Builder().setExecutorService(threadPool).setMaxRequestRetry(3).setCompressionEnforced(true)
//				.setAllowPoolingConnections(true).setAllowPoolingSslConnections(true).setConnectionTTL(-1).setConnectTimeout(5 * 1000).setFollowRedirect(true)
//				.setAsyncHttpClientProviderConfig(nettyConfig).setMaxConnectionsPerHost(settings.getConnections().getPerHost())
//				.setMaxConnections(settings.getConnections().getGlobal()).addRequestFilter(new ThrottleRequestFilter(10)).build();
//
//		this.client = new AsyncHttpClient(new NettyAsyncHttpProvider(cf), cf);
//	}
//
//	public void get(String url, Spider spider, SuccessCallback successCallback, FailureCallback failureCallback) {
//		if (StringUtils.isNotBlank(url)) {
//			log.debug("Requesting {}...", url);
//			BoundRequestBuilder request = prepareRequest(url, spider);
//
//			request.execute(new AsyncCompletionHandler<Response>() {
//				@Override
//				public Response onCompleted(Response response) throws Exception {
//					try {
//						log.debug("Getting response for {}", url);
//						WebPage webPage = extractWebPage(url, response);
//						successCallback.on(webPage);
//					} catch (Exception e) {
//						log.debug("Can not construct web page", e);
//						throw e;
//					}
//					return response;
//				}
//
//				@Override
//				public void onThrowable(Throwable t) {
//					log.trace(t.getMessage(), t);
//					failureCallback.on(t);
//				}
//			});
//		}
//	}
//
//	// TODO use RxNetty with CompletableFuture
//	@Deprecated
//	public WebPage getBlocking(String url, Spider spider) throws Exception {
//		BoundRequestBuilder request = prepareRequest(url, spider);
//		Response response = request.execute().get(5000, TimeUnit.MILLISECONDS);
//		return extractWebPage(url, response);
//	}
//
//	@Deprecated
//	public WebPage getBlocking(String url) throws Exception {
//		BoundRequestBuilder request = client.prepareGet(url);
//		Response response = request.execute().get(5000, TimeUnit.MILLISECONDS);
//		return extractWebPage(url, response);
//	}
//
//	public BoundRequestBuilder prepareRequest(String url, Spider spider) {
//		BoundRequestBuilder request = client.prepareGet(url);
//
//		// request.setInetAddress(InetAddress.)
//		// if (spider.getClient().getNameResolver() != null) {
//		// request.setNameResolver(new
//		// DelegatingNameResolver(spider.getClient().getNameResolver()));
//		// }
//
//		// Add headers, cookies and ohter stuff
//		request.setRequestTimeout(spider.getClient().getRequestTimeOut());
//		request.setFollowRedirects(spider.getClient().isFollowRedirects());
//		request.setHeaders(spider.getClient().getHeaders());
//		if (spider.getClient().getCookies() != null)
//			request.setCookies(spider
//					.getClient()
//					.getCookies()
//					.stream()
//					.map(cookie -> new Cookie(cookie.getName(), cookie.getValue(), false, cookie.getDomain(), cookie.getPath(), cookie.getExpires(), cookie
//							.getMaxAge(), cookie.isSecure(), cookie.isHttpOnly())).collect(Collectors.toList()));
//		request.setQueryParams(spider.getClient().getParams());
//
//		// Configure the proxy
//		ProxyServer proxy = spider.getClient().getProxyServersSource().findProxy(spider);
//		if (proxy != null) {
//			com.ning.http.client.ProxyServer internalProxy = new com.ning.http.client.ProxyServer(Protocol.valueOf(proxy.getProtocol().getProtocol()
//					.toUpperCase()), proxy.getHost(), proxy.getPort(), proxy.getPrincipal(), proxy.getPassword());
//			internalProxy.setCharset(proxy.getCharset());
//			internalProxy.setNtlmDomain(proxy.getNtlmDomain());
//			internalProxy.setNtlmHost(proxy.getNtlmHost());
//			internalProxy.setScheme(AuthScheme.valueOf(proxy.getScheme().name().toUpperCase()));
//			if (proxy.getNonProxyHosts() != null) {
//				proxy.getNonProxyHosts().forEach(internalProxy::addNonProxyHost);
//			}
//			request.setProxyServer(internalProxy);
//		}
//
//		// Configure the user -agent
//		String userAgent = spider.getClient().getUserAgentProvisionner().get(url, spider);
//		if (Strings.isNullOrEmpty(userAgent)) {
//			request.addHeader("User-Agent", userAgent);
//		}
//		return request;
//	}
//
//	public WebPage extractWebPage(String url, Response response) throws MalformedURLException, IOException {
//		List<io.mandrel.http.Cookie> cookies = response
//				.getCookies()
//				.stream()
//				.map(cookie -> new io.mandrel.http.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getExpires(),
//						cookie.getMaxAge(), cookie.isSecure(), cookie.isHttpOnly())).collect(Collectors.toList());
//		WebPage webPage = new WebPage(new URL(url), response.getStatusCode(), response.getStatusText(), new HashMap<>(response.getHeaders()), cookies,
//				response.getResponseBodyAsBytes());
//		return webPage;
//	}
//
//	private class DelegatingNameResolver implements com.ning.http.client.NameResolver {
//
//		private NameResolver resolver;
//
//		public DelegatingNameResolver(NameResolver resolver) {
//			this.resolver = resolver;
//		}
//
//		@Override
//		public InetAddress resolve(String name) throws UnknownHostException {
//			return resolver.resolve(name);
//		}
//	}
//}
