package io.mandrel.http.dns;

import lombok.Data;

@Data
public class InternalDnsCache implements DnsCache {

	public String optimizeUrl(String url) {
		return url;
	}
}
