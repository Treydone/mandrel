package ohscrap.client.ua;

import ohscrap.common.Spider;

public interface UserAgentProvisionner {

	String get(String url, Spider spider);
}
