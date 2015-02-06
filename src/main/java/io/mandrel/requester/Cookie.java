package io.mandrel.requester;

import lombok.Data;

@Data
public class Cookie {

	private String name;
	private String value;
	private String rawValue;
	private String domain;
	private String path;
	private long expires;
	private int maxAge;
	private boolean secure;
	private boolean httpOnly;

}
