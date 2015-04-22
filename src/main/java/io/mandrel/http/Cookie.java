package io.mandrel.http;

import java.io.Serializable;

import lombok.Data;

@Data
public class Cookie implements Serializable {

	private static final long serialVersionUID = -5911842148311031909L;

	private String name;
	private String value;
	private String domain;
	private String path;
	private long expires;
	private int maxAge;
	private boolean secure;
	private boolean httpOnly;

	public Cookie(String name, String value, String domain, String path, long expires, int maxAge, boolean secure, boolean httpOnly) {
		super();
		this.name = name;
		this.value = value;
		this.domain = domain;
		this.path = path;
		this.expires = expires;
		this.maxAge = maxAge;
		this.secure = secure;
		this.httpOnly = httpOnly;
	}

}
