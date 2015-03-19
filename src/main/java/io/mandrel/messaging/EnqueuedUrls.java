package io.mandrel.messaging;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;

@Data
public class EnqueuedUrls implements Serializable {

	private static final long serialVersionUID = 7737480486189912755L;

	private final long spiderId;

	private final Set<String> urls;

	public EnqueuedUrls(long spiderId, Set<String> urls) {
		super();
		this.spiderId = spiderId;
		this.urls = urls;
	}
}
