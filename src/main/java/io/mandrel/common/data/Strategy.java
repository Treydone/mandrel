package io.mandrel.common.data;

import io.mandrel.http.Cookie;
import io.mandrel.http.dns.DNSNameResolver;
import io.mandrel.http.dns.NameResolver;
import io.mandrel.http.proxy.NoProxyProxyServersSource;
import io.mandrel.http.proxy.ProxyServersSource;
import io.mandrel.http.ua.FixedUserAgentProvisionner;
import io.mandrel.http.ua.UserAgentProvisionner;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Strategy implements Serializable {

	private static final long serialVersionUID = 8944125504287219738L;

	@JsonProperty("request_time_out")
	private int requestTimeOut = 10000;

	@JsonProperty("max_redirects")
	private int maxRedirects = 3;

	@JsonProperty("socket_timeout")
	private int socketTimeout = 10000;

	@JsonProperty("connect_timeout")
	private int connectTimeout = 10000;

	@JsonProperty("reuse_address")
	private boolean reuseAddress = true;

	@JsonProperty("tcp_no_delay")
	private boolean tcpNoDelay = true;

	@JsonProperty("keep_alive")
	private boolean keepAlive = true;

	@JsonProperty("max_parallel")
	private int maxParallel = 100;

	@JsonProperty("max_persistent_connections")
	private int maxPersistentConnections = 100;

	@JsonProperty("headers")
	private Map<String, Collection<String>> headers;

	@JsonProperty("params")
	private Map<String, List<String>> params;

	@JsonProperty("follow_redirects")
	private boolean followRedirects = true;

	@JsonProperty("cookies")
	private List<Cookie> cookies;

	@JsonProperty("user_agent_provisionner")
	private UserAgentProvisionner userAgentProvisionner = new FixedUserAgentProvisionner("Mandrel");

	@JsonProperty("name_resolver")
	private NameResolver nameResolver = new DNSNameResolver();

	@JsonProperty("proxy")
	private ProxyServersSource proxyServersSource = new NoProxyProxyServersSource();

	@JsonProperty("politeness")
	private Politeness politeness = new Politeness();
}
