/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.http;

import io.mandrel.common.MandrelException;
import io.mandrel.common.data.Spider;
import io.mandrel.common.settings.ClientSettings;
import io.mandrel.http.proxy.ProxyServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.codecs.DefaultHttpRequestWriterFactory;
import org.apache.http.impl.nio.codecs.DefaultHttpResponseParser;
import org.apache.http.impl.nio.codecs.DefaultHttpResponseParserFactory;
import org.apache.http.impl.nio.conn.ManagedNHttpClientConnectionFactory;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.LineParser;
import org.apache.http.nio.NHttpMessageParser;
import org.apache.http.nio.NHttpMessageParserFactory;
import org.apache.http.nio.NHttpMessageWriterFactory;
import org.apache.http.nio.conn.ManagedNHttpClientConnection;
import org.apache.http.nio.conn.NHttpConnectionFactory;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.SessionInputBuffer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.CharArrayBuffer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class HCRequester extends Requester {

	@JsonIgnore
	private CloseableHttpAsyncClient client;

	@JsonIgnore
	private RequestConfig defaultRequestConfig;

	@JsonIgnore
	private Semaphore available;

	@JsonIgnore
	private ClientSettings settings;

	public void close() throws IOException {
		client.close();
	}

	public void init() {

		available = new Semaphore(strategy.getMaxParallel(), true);

		NHttpMessageParserFactory<HttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {
			@Override
			public NHttpMessageParser<HttpResponse> create(final SessionInputBuffer buffer, final MessageConstraints constraints) {
				LineParser lineParser = new BasicLineParser() {

					@Override
					public Header parseHeader(final CharArrayBuffer buffer) {
						try {
							return super.parseHeader(buffer);
						} catch (ParseException ex) {
							return new BasicHeader(buffer.toString(), null);
						}
					}
				};
				return new DefaultHttpResponseParser(buffer, lineParser, DefaultHttpResponseFactory.INSTANCE, constraints);
			}
		};
		NHttpMessageWriterFactory<HttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();

		NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory = new ManagedNHttpClientConnectionFactory(requestWriterFactory, responseParserFactory,
				HeapByteBufferAllocator.INSTANCE);

		SSLContext sslcontext = SSLContexts.createSystemDefault();
		HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();

		Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder.<SchemeIOSessionStrategy> create()
				.register("http", NoopIOSessionStrategy.INSTANCE).register("https", new SSLIOSessionStrategy(sslcontext, null, null, hostnameVerifier)).build();

		DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
			@Override
			public InetAddress[] resolve(final String host) throws UnknownHostException {
				if (host.equalsIgnoreCase("localhost")) {
					return new InetAddress[] { InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }) };
				} else {
					return new InetAddress[] { strategy.getNameResolver().resolve(host) };
				}
			}
		};

		// Create I/O reactor configuration
		IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(Runtime.getRuntime().availableProcessors())
				.setConnectTimeout(settings.getTimouts().getConnection()).setSoReuseAddress(strategy.isReuseAddress()).setSoKeepAlive(strategy.isKeepAlive())
				.setTcpNoDelay(strategy.isTcpNoDelay()).setSoTimeout(strategy.getSocketTimeout()).build();

		// Create a custom I/O reactor
		ConnectingIOReactor ioReactor;
		try {
			ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
		} catch (IOReactorException e) {
			throw Throwables.propagate(e);
		}

		// Create a connection manager with custom configuration.
		PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor, connFactory, sessionStrategyRegistry, dnsResolver);

		// Create message constraints
		// TODO
		MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(-1).setMaxLineLength(-1).build();

		// Create connection configuration
		ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
				.setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).setMessageConstraints(messageConstraints).build();
		connManager.setDefaultConnectionConfig(connectionConfig);

		// Configure total max or per route limits for persistent connections
		// that can be kept in the pool or leased by the connection manager.
		connManager.setMaxTotal(strategy.getMaxPersistentConnections());
		connManager.setDefaultMaxPerRoute(strategy.getMaxPersistentConnections());

		// TODO
		// Use custom credentials provider if necessary.
		// CredentialsProvider credentialsProvider = new
		// BasicCredentialsProvider();

		// Create global request configuration
		defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).setExpectContinueEnabled(true).setStaleConnectionCheckEnabled(true)
				.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
				.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).setMaxRedirects(strategy.getMaxRedirects())
				.setSocketTimeout(strategy.getSocketTimeout()).setConnectTimeout(strategy.getConnectTimeout())
				.setConnectionRequestTimeout(strategy.getRequestTimeOut()).setRedirectsEnabled(strategy.isFollowRedirects()).build();

		// Create an HttpClient with the given custom dependencies and
		// configuration.
		client = HttpAsyncClients.custom().setConnectionManager(connManager)
		// .setDefaultCredentialsProvider(credentialsProvider)
				.setDefaultRequestConfig(defaultRequestConfig).build();

		client.start();
	}

	public void get(String url, Spider spider, SuccessCallback successCallback, FailureCallback failureCallback) {
		if (StringUtils.isNotBlank(url)) {
			log.debug("Requesting {}...", url);

			try {
				if (available.tryAcquire(Integer.MAX_VALUE, TimeUnit.MILLISECONDS)) {
					HttpUriRequest request = prepareRequest(url, spider);
					HttpContext localContext = prepareContext(spider);

					client.execute(request, localContext, new FutureCallback<HttpResponse>() {
						@Override
						public void failed(Exception ex) {
							try {
								log.trace(ex.getMessage(), ex);
								failureCallback.on(ex);
							} finally {
								available.release();
							}
						}

						@Override
						public void completed(HttpResponse result) {
							try {
								log.debug("Getting response for {}", url);
								WebPage webPage = extractWebPage(url, result, localContext);
								successCallback.on(webPage);
							} catch (Exception e) {
								log.debug("Can not construct web page", e);
								throw Throwables.propagate(e);
							} finally {
								available.release();
							}
						}

						@Override
						public void cancelled() {
							try {
								log.warn("Cancelled");
								failureCallback.on(null);
							} finally {
								available.release();
							}
						}
					});
				} else {
					throw new MandrelException("Can not acquire lock, too many connection!");
				}
			} catch (InterruptedException e) {
				throw Throwables.propagate(e);
			}

		}
	}

	public HttpContext prepareContext(Spider spider) {
		CookieStore store = new BasicCookieStore();
		if (strategy.getCookies() != null)
			strategy.getCookies().forEach(cookie -> {
				BasicClientCookie theCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
				theCookie.setDomain(cookie.getDomain());
				theCookie.setPath(cookie.getPath());
				theCookie.setExpiryDate(new Date(cookie.getExpires()));
				theCookie.setSecure(cookie.isSecure());
				store.addCookie(theCookie);
			});

		HttpContext localContext = new BasicHttpContext();
		localContext.setAttribute(HttpClientContext.COOKIE_STORE, store);
		return localContext;
	}

	// TODO use RxNetty with CompletableFuture
	@Deprecated
	public WebPage getBlocking(String url, Spider spider) throws Exception {
		HttpUriRequest request = prepareRequest(url, spider);
		HttpResponse response = client.execute(request, null).get(5000, TimeUnit.MILLISECONDS);
		return extractWebPage(url, response, null);
	}

	@Deprecated
	public WebPage getBlocking(String url) throws Exception {
		HttpResponse response = client.execute(new HttpGet(url), null).get(5000, TimeUnit.MILLISECONDS);
		return extractWebPage(url, response, null);
	}

	public HttpUriRequest prepareRequest(String url, Spider spider) {
		Builder builder = RequestConfig.copy(defaultRequestConfig);

		HttpGet request = new HttpGet(url);

		// Add headers, cookies and ohter stuff
		if (strategy.getHeaders() != null) {
			strategy.getHeaders().forEach((k, v) -> {
				if (v != null) {
					v.forEach(el -> request.addHeader(k, el));
				} else {
					request.addHeader(k, null);
				}
			});
		}

		HttpParams params = new BasicHttpParams();
		if (strategy.getParams() != null) {
			strategy.getParams().forEach((k, v) -> {
				if (v != null) {
					v.forEach(el -> params.setParameter(k, el));
				} else {
					params.setParameter(k, null);
				}
			});
		}
		request.setParams(params);

		// Configure the user -agent
		String userAgent = strategy.getUserAgentProvisionner().get(url, spider);
		if (Strings.isNullOrEmpty(userAgent)) {
			request.addHeader("User-Agent", userAgent);
		}

		// Configure the proxy
		ProxyServer proxy = strategy.getProxyServersSource().findProxy(spider);
		if (proxy != null) {
			// TODO Auth!
			HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol().getProtocol());
			builder.setProxy(proxyHost);
		}

		request.setConfig(builder.build());
		return request;
	}

	public WebPage extractWebPage(String url, HttpResponse result, HttpContext localContext) throws MalformedURLException, IOException {
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		if (result.getAllHeaders() != null) {
			for (Header header : result.getAllHeaders()) {
				headers.put(header.getName(), Arrays.asList(header.getValue()));
			}
		}

		CookieStore store = (CookieStore) localContext.getAttribute(HttpClientContext.COOKIE_STORE);
		List<io.mandrel.http.Cookie> cookies = null;
		if (store.getCookies() != null) {
			cookies = store
					.getCookies()
					.stream()
					.filter(cookie -> cookie != null)
					.map(cookie -> new io.mandrel.http.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(),
							cookie.getExpiryDate() != null ? cookie.getExpiryDate().getTime() : 0, cookie.getExpiryDate() != null ? (int) cookie
									.getExpiryDate().getTime() : 0, cookie.isSecure(), false)).collect(Collectors.toList());
		}

		WebPage webPage = new WebPage(new URL(url), result.getStatusLine() != null ? result.getStatusLine().getStatusCode() : 0,
				result.getStatusLine() != null ? result.getStatusLine().getReasonPhrase() : null, headers, cookies,
				result.getEntity() != null ? IOUtils.toByteArray(result.getEntity().getContent()) : null);
		return webPage;
	}

	@Override
	public String getType() {
		return "hc";
	}
}
