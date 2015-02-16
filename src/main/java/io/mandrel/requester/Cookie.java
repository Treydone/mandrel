package io.mandrel.requester;

import java.io.Serializable;

import lombok.Data;

@Data
public class Cookie implements Serializable {

	private static final long serialVersionUID = -5911842148311031909L;

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
