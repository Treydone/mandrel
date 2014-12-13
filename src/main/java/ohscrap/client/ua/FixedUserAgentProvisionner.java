package ohscrap.client.ua;

import ohscrap.common.Spider;

public class FixedUserAgentProvisionner implements UserAgentProvisionner {

	private final String ua;

	public FixedUserAgentProvisionner(String ua) {
		super();
		this.ua = ua;
	}

	public String get(String url, Spider spider) {
		return ua;
	}
}
