package io.mandrel.http.dns;

import lombok.Data;

@Data
public class InternalDnsCache implements DnsCache {

	private static final long serialVersionUID = -7534644889369417852L;

	public String optimizeUrl(String url) {
		return url;
	}
}
