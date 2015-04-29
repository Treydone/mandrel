package io.mandrel.http.ua;

import lombok.Data;
import io.mandrel.common.data.Spider;

@Data
public class FixedUserAgentProvisionner implements UserAgentProvisionner {

	private static final long serialVersionUID = -8868530554024953901L;

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
