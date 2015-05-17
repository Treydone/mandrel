package io.mandrel.http;

import io.mandrel.common.data.Spider;
import io.mandrel.common.settings.ClientSettings;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
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
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.CharArrayBuffer;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;

@Component
@Slf4j
public class HCRequester implements Requester, Closeable {

	private final CloseableHttpAsyncClient client;

	private final RequestConfig defaultRequestConfig;

	@Override
	public void close() throws IOException {
		client.close();
	}

	@Inject
	public HCRequester(ClientSettings settings) throws IOReactorException {

		// Use custom message parser / writer to customize the way HTTP
		// messages are parsed from and written out to the data stream.
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

		// Use a custom connection factory to customize the process of
		// initialization of outgoing HTTP connections. Beside standard
		// connection
		// configuration parameters HTTP connection factory can define message
		// parser / writer routines to be employed by individual connections.
		NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory = new ManagedNHttpClientConnectionFactory(requestWriterFactory, responseParserFactory,
				HeapByteBufferAllocator.INSTANCE);

		// Client HTTP connection objects when fully initialized can be bound to
		// an arbitrary network socket. The process of network socket
		// initialization, its connection to a remote address and binding to a
		// local one is
		// controlled by a connection socket factory.

		// SSL context for secure connections can be created either based on
		// system or application specific properties.
		SSLContext sslcontext = SSLContexts.createSystemDefault();
		// Use custom hostname verifier to customize SSL hostname verification.
		HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();

		// Create a registry of custom connection session strategies for
		// supported
		// protocol schemes.
		Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder.<SchemeIOSessionStrategy> create()
				.register("http", NoopIOSessionStrategy.INSTANCE).register("https", new SSLIOSessionStrategy(sslcontext, null, null, hostnameVerifier)).build();

		// Use custom DNS resolver to override the system DNS resolution.
		DnsResolver dnsResolver = new SystemDefaultDnsResolver() {

			@Override
			public InetAddress[] resolve(final String host) throws UnknownHostException {
				if (host.equalsIgnoreCase("localhost")) {
					return new InetAddress[] { InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }) };
				} else {
					return super.resolve(host);
				}
			}

		};

		// Create I/O reactor configuration
		IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(Runtime.getRuntime().availableProcessors()).setConnectTimeout(30000)
				.setSoTimeout(30000).build();

		// Create a custom I/O reactort
		ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

		// Create a connection manager with custom configuration.
		PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor, connFactory, sessionStrategyRegistry, dnsResolver);

		// Create message constraints
		// TODO
		MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(-1).setMaxLineLength(-1).build();
		// Create connection configuration
		ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
				.setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).setMessageConstraints(messageConstraints).build();
		// Configure the connection manager to use connection configuration
		// either by default or for a specific host.
		connManager.setDefaultConnectionConfig(connectionConfig);
		// TODO
		// connManager.setConnectionConfig(new HttpHost("somehost", 80),
		// ConnectionConfig.DEFAULT);

		// Configure total max or per route limits for persistent connections
		// that can be kept in the pool or leased by the connection manager.
		connManager.setMaxTotal(100);
		connManager.setDefaultMaxPerRoute(100);
		// TODO
		// connManager.setMaxPerRoute(new HttpRoute(new HttpHost("somehost",
		// 80)), 20);

		// Use custom cookie store if necessary.
		// CookieStore cookieStore = new BasicCookieStore();
		// Use custom credentials provider if necessary.
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		// Create global request configuration
		defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).setExpectContinueEnabled(true).setStaleConnectionCheckEnabled(true)
				.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
				.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).setMaxRedirects(3).setSocketTimeout(10000).setConnectTimeout(10000).build();

		// Create an HttpClient with the given custom dependencies and
		// configuration.
		client = HttpAsyncClients.custom().setConnectionManager(connManager)
		// .setDefaultCookieStore(cookieStore)
				.setDefaultCredentialsProvider(credentialsProvider).setDefaultRequestConfig(defaultRequestConfig).build();

		client.start();
	}

	public void get(String url, Spider spider, SuccessCallback successCallback, FailureCallback failureCallback) {
		if (StringUtils.isNotBlank(url)) {
			log.debug("Requesting {}...", url);

			HttpUriRequest request = prepareRequest(url, spider);
			client.execute(request, new FutureCallback<HttpResponse>() {
				@Override
				public void failed(Exception ex) {
					log.trace(ex.getMessage(), ex);
					failureCallback.on(ex);
				}

				@Override
				public void completed(HttpResponse result) {
					try {
						log.debug("Getting response for {}", url);
						WebPage webPage = extractWebPage(url, result);
						successCallback.on(webPage);
					} catch (Exception e) {
						log.debug("Can not construct web page", e);
						throw Throwables.propagate(e);
					}
				}

				@Override
				public void cancelled() {
					log.warn("Cancelled");
					failureCallback.on(null);
				}
			});
		}
	}

	// TODO use RxNetty with CompletableFuture
	@Deprecated
	public WebPage getBlocking(String url, Spider spider) throws Exception {
		HttpUriRequest request = prepareRequest(url, spider);
		HttpResponse response = client.execute(request, null).get(5000, TimeUnit.MILLISECONDS);
		return extractWebPage(url, response);
	}

	@Deprecated
	public WebPage getBlocking(String url) throws Exception {
		HttpResponse response = client.execute(new HttpGet(url), null).get(5000, TimeUnit.MILLISECONDS);
		return extractWebPage(url, response);
	}

	public HttpUriRequest prepareRequest(String url, Spider spider) {

		HttpGet request = new HttpGet(url);
		// Request configuration can be overridden at the request level.
		// They will take precedence over the one set at the client level.
		RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).setSocketTimeout(5000).setConnectTimeout(5000).setConnectionRequestTimeout(5000)
		// .setProxy(new HttpHost("myotherproxy", 8080))
				.build();
		request.setConfig(requestConfig);

		// request.setInetAddress(InetAddress.)
		// if (spider.getClient().getNameResolver() != null) {
		// request.setNameResolver(new
		// DelegatingNameResolver(spider.getClient().getNameResolver()));
		// }

		// Add headers, cookies and ohter stuff
		// request.setRequestTimeout(spider.getClient().getRequestTimeOut());
		// request.setFollowRedirects(spider.getClient().isFollowRedirects());
		// request.setHeaders(spider.getClient().getHeaders());
		// if (spider.getClient().getCookies() != null)
		// request.setCookies(spider
		// .getClient()
		// .getCookies()
		// .stream()
		// .map(cookie -> new Cookie(cookie.getName(), cookie.getValue(), false,
		// cookie.getDomain(), cookie.getPath(), cookie.getExpires(), cookie
		// .getMaxAge(), cookie.isSecure(),
		// cookie.isHttpOnly())).collect(Collectors.toList()));
		// request.setQueryParams(spider.getClient().getParams());
		//
		// // Configure the proxy
		// ProxyServer proxy =
		// spider.getClient().getProxyServersSource().findProxy(spider);
		// if (proxy != null) {
		// com.ning.http.client.ProxyServer internalProxy = new
		// com.ning.http.client.ProxyServer(Protocol.valueOf(proxy.getProtocol().getProtocol()
		// .toUpperCase()), proxy.getHost(), proxy.getPort(),
		// proxy.getPrincipal(), proxy.getPassword());
		// internalProxy.setCharset(proxy.getCharset());
		// internalProxy.setNtlmDomain(proxy.getNtlmDomain());
		// internalProxy.setNtlmHost(proxy.getNtlmHost());
		// internalProxy.setScheme(AuthScheme.valueOf(proxy.getScheme().name().toUpperCase()));
		// if (proxy.getNonProxyHosts() != null) {
		// proxy.getNonProxyHosts().forEach(internalProxy::addNonProxyHost);
		// }
		// request.setProxyServer(internalProxy);
		// }
		//
		// // Configure the user -agent
		// String userAgent =
		// spider.getClient().getUserAgentProvisionner().get(url, spider);
		// if (Strings.isNullOrEmpty(userAgent)) {
		// request.addHeader("User-Agent", userAgent);
		// }
		return request;
	}

	public WebPage extractWebPage(String url, HttpResponse result) throws MalformedURLException, IOException {
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		for (Header header : result.getAllHeaders()) {
			headers.put(header.getName(), Arrays.asList(header.getValue()));
		}

		// List<io.mandrel.http.Cookie> cookies = result
		// .getCookies()
		// .stream()
		// .map(cookie -> new io.mandrel.http.Cookie(cookie.getName(),
		// cookie.getValue(), cookie.getDomain(), cookie.getPath(),
		// cookie.getExpires(),
		// cookie.getMaxAge(), cookie.isSecure(),
		// cookie.isHttpOnly())).collect(Collectors.toList());
		WebPage webPage = new WebPage(new URL(url), result.getStatusLine().getStatusCode(), result.getStatusLine().getReasonPhrase(), headers, null,
				IOUtils.toByteArray(result.getEntity().getContent()));
		return webPage;
	}

}
