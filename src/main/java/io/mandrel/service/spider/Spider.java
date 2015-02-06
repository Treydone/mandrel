package io.mandrel.service.spider;

import io.mandrel.common.content.WebPageExtractor;
import io.mandrel.common.filters.WebPageFilter;
import io.mandrel.common.source.Source;
import io.mandrel.common.store.PageMetadataStore;
import io.mandrel.common.store.WebPageStore;
import io.mandrel.common.store.impl.InternalStore;
import io.mandrel.requester.Cookie;
import io.mandrel.requester.dns.DnsCache;
import io.mandrel.requester.dns.InternalDnsCache;
import io.mandrel.requester.ua.FixedUserAgentProvisionner;
import io.mandrel.requester.ua.UserAgentProvisionner;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Spider {
	private long id;
	private State state = State.NEW;
	@NotNull
	private List<Source> sources;
	private List<WebPageFilter> filters;
	private List<WebPageExtractor> extractors;
	private Stores stores = new Stores();
	private Client client = new Client();

	@Data
	public static class Stores {
		@JsonProperty("metadata_store")
		private PageMetadataStore pageMetadataStore = new InternalStore();
		@JsonProperty("page_store")
		private WebPageStore pageStore = new InternalStore();
	}

	@Data
	public static class Client {
		@JsonProperty("request_time_out")
		private int requestTimeOut = 3000;
		private Map<String, Collection<String>> headers;
		private Map<String, List<String>> params;
		@JsonProperty("time_out")
		private boolean followRedirects = false;
		private List<Cookie> cookies;
		@JsonProperty("user_agent_provisionner")
		private UserAgentProvisionner userAgentProvisionner = new FixedUserAgentProvisionner("Mandrel");
		@JsonProperty("dns_cache")
		private DnsCache dnsCache = new InternalDnsCache();
	}

	public enum State {
		NEW, STARTED, PAUSED, STOPPED, CANCELLED
	}
}
