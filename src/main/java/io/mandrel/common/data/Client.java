package io.mandrel.common.data;

import io.mandrel.http.Cookie;
import io.mandrel.http.dns.CachedNameResolver;
import io.mandrel.http.dns.NameResolver;
import io.mandrel.http.proxy.NoProxyProxyServersSource;
import io.mandrel.http.proxy.ProxyServersSource;
import io.mandrel.http.ua.FixedUserAgentProvisionner;
import io.mandrel.http.ua.UserAgentProvisionner;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Client implements Serializable {

	private static final long serialVersionUID = -4242505953994309024L;

	@JsonProperty("request_time_out")
	private int requestTimeOut = 10000;

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
	private NameResolver nameResolver = new CachedNameResolver();

	@JsonProperty("proxy")
	private ProxyServersSource proxyServersSource = new NoProxyProxyServersSource();

	@JsonProperty("politeness")
	private Politeness politeness = new Politeness();
}