package io.mandrel.common.data;

import io.mandrel.requester.Cookie;
import io.mandrel.requester.dns.DnsCache;
import io.mandrel.requester.dns.InternalDnsCache;
import io.mandrel.requester.proxy.NoProxyProxyServersSource;
import io.mandrel.requester.proxy.ProxyServersSource;
import io.mandrel.requester.ua.FixedUserAgentProvisionner;
import io.mandrel.requester.ua.UserAgentProvisionner;

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
	private int requestTimeOut = 3000;

	@JsonProperty("headers")
	private Map<String, Collection<String>> headers;

	@JsonProperty("params")
	private Map<String, List<String>> params;

	@JsonProperty("follow_redirects")
	private boolean followRedirects = false;

	@JsonProperty("cookies")
	private List<Cookie> cookies;

	@JsonProperty("user_agent_provisionner")
	private UserAgentProvisionner userAgentProvisionner = new FixedUserAgentProvisionner("Mandrel");

	@JsonProperty("dns_cache")
	private DnsCache dnsCache = new InternalDnsCache();

	@JsonProperty("proxy")
	private ProxyServersSource proxyServersSource = new NoProxyProxyServersSource();

	@JsonProperty("politeness")
	private Politeness politeness = new Politeness();
}