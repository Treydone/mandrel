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
package io.mandrel.requests.http;

import io.mandrel.blob.Blob;
import io.mandrel.blob.BlobMetadata;
import io.mandrel.common.data.HttpStrategy;
import io.mandrel.common.data.HttpStrategy.HttpStrategyDefinition;
import io.mandrel.common.data.Spider;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.requests.Requester;
import io.mandrel.requests.proxy.ProxyServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class ApacheHttpRequester extends Requester<HttpStrategy> {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class ApacheHttpRequesterDefinition extends RequesterDefinition<HttpStrategy, ApacheHttpRequester> {

		private static final long serialVersionUID = -9205125497698919267L;

		@JsonProperty("max_line_length")
		private int maxLineLength = -1;

		@JsonProperty("max_header_count")
		private int maxHeaderCount = -1;

		@JsonProperty("io_thread_count")
		private int ioThreadCount = Runtime.getRuntime().availableProcessors();

		public ApacheHttpRequesterDefinition() {
			setStrategy(new HttpStrategyDefinition());
		}

		@Override
		public String name() {
			return "hc";
		}

		@Override
		public ApacheHttpRequester build(TaskContext context) {
			return build(new ApacheHttpRequester(context).maxLineLength(maxLineLength).maxHeaderCount(maxHeaderCount).ioThreadCount(ioThreadCount), context);
		}

	}

	public ApacheHttpRequester() {
		super(null);
	}

	public ApacheHttpRequester(TaskContext context) {
		super(context);
	}

	private int maxLineLength = -1;

	private int maxHeaderCount = -1;

	private int ioThreadCount = Runtime.getRuntime().availableProcessors();

	// //////////////////////////////////////
	private CloseableHttpAsyncClient client;

	private RequestConfig defaultRequestConfig;

	private Semaphore available;

	public void close() throws IOException {
		client.close();
	}

	public void init() {

		available = new Semaphore(strategy().maxParallel(), true);

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
					return new InetAddress[] { strategy().nameResolver().resolve(host) };
				}
			}
		};

		// Create I/O reactor configuration

		IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(ioThreadCount).setConnectTimeout(strategy().connectTimeout())
				.setSoReuseAddress(strategy().reuseAddress()).setSoKeepAlive(strategy().keepAlive()).setTcpNoDelay(strategy().tcpNoDelay())
				.setSoTimeout(strategy().socketTimeout()).build();

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
		MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(maxHeaderCount).setMaxLineLength(maxLineLength).build();

		// Create connection configuration
		ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
				.setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).setMessageConstraints(messageConstraints).build();
		connManager.setDefaultConnectionConfig(connectionConfig);

		// Configure total max or per route limits for persistent connections
		// that can be kept in the pool or leased by the connection manager.
		connManager.setMaxTotal(strategy().maxPersistentConnections());
		connManager.setDefaultMaxPerRoute(strategy().maxPersistentConnections());

		// TODO
		// Use custom credentials provider if necessary.
		// CredentialsProvider credentialsProvider = new
		// BasicCredentialsProvider();

		// Create global request configuration
		defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).setExpectContinueEnabled(true).setStaleConnectionCheckEnabled(true)
				.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
				.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).setMaxRedirects(strategy().maxRedirects())
				.setSocketTimeout(strategy().socketTimeout()).setConnectTimeout(strategy().connectTimeout())
				.setConnectionRequestTimeout(strategy().requestTimeOut()).setRedirectsEnabled(strategy().followRedirects()).build();

		// Create an HttpClient with the given custom dependencies and
		// configuration.
		client = HttpAsyncClients.custom().setConnectionManager(connManager)
		// .setDefaultCredentialsProvider(credentialsProvider)
				.setDefaultRequestConfig(defaultRequestConfig).build();

		client.start();
	}

	// public void get(Uri uri, Spider spider, SuccessCallback successCallback,
	// FailureCallback failureCallback) {
	// if (uri != null) {
	// log.debug("Requesting {}...", uri);
	//
	// try {
	// if (available.tryAcquire(Integer.MAX_VALUE, TimeUnit.MILLISECONDS)) {
	// HttpUriRequest request = prepareRequest(uri, spider);
	// HttpContext localContext = prepareContext(spider);
	//
	// client.execute(request, localContext, new FutureCallback<HttpResponse>()
	// {
	// @Override
	// public void failed(Exception ex) {
	// try {
	// log.trace(ex.getMessage(), ex);
	// failureCallback.on(ex);
	// } finally {
	// available.release();
	// }
	// }
	//
	// @Override
	// public void completed(HttpResponse result) {
	// try {
	// log.debug("Getting response for {}", uri);
	// Blob webPage = extractWebPage(uri, result, localContext);
	// successCallback.on(webPage);
	// } catch (Exception e) {
	// log.debug("Can not construct web page", e);
	// throw Throwables.propagate(e);
	// } finally {
	// available.release();
	// }
	// }
	//
	// @Override
	// public void cancelled() {
	// try {
	// log.warn("Cancelled");
	// failureCallback.on(null);
	// } finally {
	// available.release();
	// }
	// }
	// });
	// } else {
	// throw new MandrelException("Can not acquire lock, too many connection!");
	// }
	// } catch (InterruptedException e) {
	// throw Throwables.propagate(e);
	// }
	// }
	// }

	public HttpContext prepareContext(Spider spider) {
		CookieStore store = new BasicCookieStore();
		if (strategy().cookies() != null)
			strategy().cookies().forEach(cookie -> {
				BasicClientCookie theCookie = new BasicClientCookie(cookie.name(), cookie.value());
				theCookie.setDomain(cookie.domain());
				theCookie.setPath(cookie.path());
				theCookie.setExpiryDate(new Date(cookie.expires()));
				theCookie.setSecure(cookie.secure());
				store.addCookie(theCookie);
			});

		HttpContext localContext = new BasicHttpContext();
		localContext.setAttribute(HttpClientContext.COOKIE_STORE, store);
		return localContext;
	}

	public Blob get(Uri uri, Spider spider) throws Exception {
		HttpUriRequest request = prepareRequest(uri, spider);
		HttpResponse response = client.execute(request, null).get(5000, TimeUnit.MILLISECONDS);
		return extractWebPage(uri, response, null);
	}

	public Blob get(Uri uri) throws Exception {
		HttpResponse response = client.execute(new HttpGet(uri.toURI()), null).get(5000, TimeUnit.MILLISECONDS);
		return extractWebPage(uri, response, null);
	}

	public HttpUriRequest prepareRequest(Uri uri, Spider spider) {
		Builder builder = RequestConfig.copy(defaultRequestConfig);

		HttpGet request = new HttpGet(uri.toURI());

		// Add headers, cookies and ohter stuff
		if (strategy().headers() != null) {
			strategy().headers().forEach(header -> {
				if (header != null) {
					request.addHeader(header.getName(), header.getValue());
				}
			});
		}

		HttpParams params = new BasicHttpParams();
		if (strategy().params() != null) {
			strategy().params().forEach(param -> {
				if (param != null) {
					params.setParameter(param.getName(), param.getValue());
				}
			});
		}
		request.setParams(params);

		// Configure the user -agent
		String userAgent = strategy().userAgentProvisionner().get(uri.toString(), spider);
		if (Strings.isNullOrEmpty(userAgent)) {
			request.addHeader("User-Agent", userAgent);
		}

		// Configure the proxy
		ProxyServer proxy = strategy().proxyServersSource().findProxy(spider);
		if (proxy != null) {
			// TODO Auth!
			HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol().getProtocol());
			builder.setProxy(proxyHost);
		}

		request.setConfig(builder.build());
		return request;
	}

	public Blob extractWebPage(Uri uri, HttpResponse result, HttpContext localContext) throws MalformedURLException, IOException {
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		if (result.getAllHeaders() != null) {
			for (Header header : result.getAllHeaders()) {
				headers.put(header.getName(), Arrays.asList(header.getValue()));
			}
		}

		List<io.mandrel.requests.http.Cookie> cookies = null;
		if (localContext != null) {
			CookieStore store = (CookieStore) localContext.getAttribute(HttpClientContext.COOKIE_STORE);
			if (store.getCookies() != null) {
				cookies = store
						.getCookies()
						.stream()
						.filter(cookie -> cookie != null)
						.map(cookie -> new io.mandrel.requests.http.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie
								.getExpiryDate() != null ? cookie.getExpiryDate().getTime() : 0, cookie.getExpiryDate() != null ? (int) cookie.getExpiryDate()
								.getTime() : 0, cookie.isSecure(), false)).collect(Collectors.toList());
			}
		}

		HttpFetchMetadata metadata = new HttpFetchMetadata().headers(headers).cookies(cookies);
		metadata.setUri(uri).setStatusCode(result.getStatusLine() != null ? result.getStatusLine().getStatusCode() : 0)
				.setStatusText(result.getStatusLine() != null ? result.getStatusLine().getReasonPhrase() : null);

		long contentLength = result.getEntity().getContentLength();
		Blob blob = new Blob(new BlobMetadata().setUri(uri).setSize(contentLength < 0 ? null : contentLength).setFetchMetadata(metadata)).payload(result
				.getEntity().getContent());
		return blob;
	}

	@Override
	public Set<String> getProtocols() {
		return Sets.newHashSet("http", "https");
	}

	@Override
	public boolean check() {
		// TODO
		return true;
	}
}
