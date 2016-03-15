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
import io.mandrel.common.data.Param;
import io.mandrel.common.data.Spider;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.requests.RequestException;
import io.mandrel.requests.Requester;
import io.mandrel.requests.http.ua.FixedUserAgentProvisionner.FixedUserAgentProvisionnerDefinition;
import io.mandrel.requests.http.ua.UserAgentProvisionner;
import io.mandrel.requests.http.ua.UserAgentProvisionner.UserAgentProvisionnerDefinition;
import io.mandrel.requests.proxy.ProxyServer;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class ApacheHttpRequester extends Requester {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class ApacheHttpRequesterDefinition extends RequesterDefinition<ApacheHttpRequester> {

		private static final long serialVersionUID = -9205125497698919267L;

		@JsonProperty("max_line_length")
		private int maxLineLength = -1;

		@JsonProperty("max_header_count")
		private int maxHeaderCount = -1;

		@JsonProperty("io_thread_count")
		private int ioThreadCount = Runtime.getRuntime().availableProcessors();

		@JsonProperty("max_redirects")
		private int maxRedirects = 3;

		@JsonProperty("follow_redirects")
		private boolean followRedirects = true;

		@JsonProperty("headers")
		private Set<Header> headers;

		@JsonProperty("params")
		private Set<Param> params;

		@JsonProperty("cookies")
		private List<Cookie> cookies;

		@JsonProperty("user_agent_provisionner")
		private UserAgentProvisionnerDefinition userAgentProvisionner = new FixedUserAgentProvisionnerDefinition();

		@Override
		public String name() {
			return "hc";
		}

		@Override
		public ApacheHttpRequester build(TaskContext context) {
			ApacheHttpRequester apacheHttpRequester = new ApacheHttpRequester(context);
			return build(apacheHttpRequester.maxLineLength(maxLineLength).maxHeaderCount(maxHeaderCount).cookies(cookies).followRedirects(followRedirects)
					.headers(headers).maxRedirects(maxRedirects).params(params).userAgentProvisionner(userAgentProvisionner.build(context)), context);
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
	private int maxRedirects;
	private boolean followRedirects;
	private Set<Header> headers;
	private Set<Param> params;
	private List<Cookie> cookies;
	private UserAgentProvisionner userAgentProvisionner;

	// //////////////////////////////////////
	private CloseableHttpClient client;

	private RequestConfig defaultRequestConfig;

	private Semaphore available;

	public void close() throws IOException {
		client.close();
	}

	public void init() {

		available = new Semaphore(maxParallel(), true);

		SSLContext sslContext = SSLContexts.createSystemDefault();
		HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();

		Registry<ConnectionSocketFactory> sessionStrategyRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.register("https", new SSLConnectionSocketFactory(sslContext, hostnameVerifier)).build();

		DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
			@Override
			public InetAddress[] resolve(final String host) throws UnknownHostException {
				if (host.equalsIgnoreCase("localhost")) {
					return new InetAddress[] { InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }) };
				} else {
					return new InetAddress[] { nameResolver().resolve(host) };
				}
			}
		};

		// Create a connection manager with custom configuration.
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(sessionStrategyRegistry, dnsResolver);

		// Create message constraints
		MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(maxHeaderCount).setMaxLineLength(maxLineLength).build();

		// Create connection configuration
		ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
				.setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).setMessageConstraints(messageConstraints).build();
		connManager.setDefaultConnectionConfig(connectionConfig);

		// Configure total max or per route limits for persistent connections
		// that can be kept in the pool or leased by the connection manager.
		connManager.setMaxTotal(maxPersistentConnections());
		connManager.setDefaultMaxPerRoute(maxPersistentConnections());

		// TODO
		// Use custom credentials provider if necessary.
		// CredentialsProvider credentialsProvider = new
		// BasicCredentialsProvider();

		// Create global request configuration
		defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).setExpectContinueEnabled(true).setStaleConnectionCheckEnabled(true)
				.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
				.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).setMaxRedirects(maxRedirects()).setSocketTimeout(socketTimeout())
				.setConnectTimeout(connectTimeout()).setConnectionRequestTimeout(requestTimeOut()).setRedirectsEnabled(followRedirects()).build();

		// Create an HttpClient with the given custom dependencies and
		// configuration.
		client = HttpClients.custom().setConnectionManager(connManager)
		// .setDefaultCredentialsProvider(credentialsProvider)
				.setDefaultRequestConfig(defaultRequestConfig).build();
	}

	public HttpContext prepareContext(Spider spider) {
		CookieStore store = new BasicCookieStore();
		if (cookies() != null)
			cookies().forEach(cookie -> {
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
		CloseableHttpResponse response;
		try {
			response = client.execute(request);
		} catch (ConnectTimeoutException e) {
			throw new io.mandrel.requests.ConnectTimeoutException(e);
		} catch (IOException e) {
			throw new RequestException(e);
		}
		return extractWebPage(uri, response, null);
	}

	public Blob get(Uri uri) throws Exception {
		CloseableHttpResponse response = client.execute(new HttpGet(uri.toURI()));
		return extractWebPage(uri, response, null);
	}

	public HttpUriRequest prepareRequest(Uri uri, Spider spider) {
		Builder builder = RequestConfig.copy(defaultRequestConfig);

		HttpGet request = new HttpGet(uri.toURI());

		// Add headers, cookies and ohter stuff
		if (headers() != null) {
			headers().forEach(header -> {
				if (header != null) {
					request.addHeader(header.getName(), header.getValue());
				}
			});
		}

		HttpParams params = new BasicHttpParams();
		if (params() != null) {
			params().forEach(param -> {
				if (param != null) {
					params.setParameter(param.getName(), param.getValue());
				}
			});
		}
		request.setParams(params);

		// Configure the user -agent
		String userAgent = userAgentProvisionner().get(uri.toString(), spider);
		if (Strings.isNullOrEmpty(userAgent)) {
			request.addHeader(HttpHeaders.USER_AGENT, userAgent);
		}

		// Configure the proxy
		ProxyServer proxy = proxyServersSource().findProxy(spider);
		if (proxy != null) {
			// TODO Auth!
			HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol().getProtocol());
			builder.setProxy(proxyHost);
		}

		request.setConfig(builder.build());
		return request;
	}

	public Blob extractWebPage(Uri uri, CloseableHttpResponse result, HttpContext localContext) throws MalformedURLException, IOException {
		try {
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
							.map(cookie -> new io.mandrel.requests.http.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(),
									cookie.getExpiryDate() != null ? cookie.getExpiryDate().getTime() : 0, cookie.getExpiryDate() != null ? (int) cookie
											.getExpiryDate().getTime() : 0, cookie.isSecure(), false)).collect(Collectors.toList());
				}
			}

			HttpFetchMetadata metadata = new HttpFetchMetadata().headers(headers).cookies(cookies);
			metadata.setUri(uri).setStatusCode(result.getStatusLine() != null ? result.getStatusLine().getStatusCode() : 0)
					.setStatusText(result.getStatusLine() != null ? result.getStatusLine().getReasonPhrase() : null);

			HttpEntity entity = result.getEntity();
			InputStream content = entity.getContent();
			try {
				long contentLength = entity.getContentLength();
				Blob blob = new Blob(new BlobMetadata().setUri(uri).setSize(contentLength < 0 ? null : contentLength).setFetchMetadata(metadata))
						.payload(IOUtils.toByteArray(content));
				return blob;
			} catch (IOException ex) {
				// In case of an IOException the connection will be released
				// back to the connection manager automatically
				throw ex;
			} finally {
				// Closing the input stream will trigger connection release
				content.close();
			}
		} finally {
			result.close();
		}
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
