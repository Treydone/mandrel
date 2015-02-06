package io.mandrel.requester.dns;

import lombok.Data;

@Data
public class InternalDnsCache implements DnsCache {

	public String optimizeUrl(String url) {
		return url;
	}
}
