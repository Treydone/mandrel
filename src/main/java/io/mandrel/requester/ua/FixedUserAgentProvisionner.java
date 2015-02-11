package io.mandrel.requester.ua;

import lombok.Data;
import io.mandrel.common.data.Spider;

@Data
public class FixedUserAgentProvisionner implements UserAgentProvisionner {

	private String ua;

	public FixedUserAgentProvisionner() {
	}

	public FixedUserAgentProvisionner(String ua) {
		this.ua = ua;
	}

	public String get(String url, Spider spider) {
		return ua;
	}

}
