package io.mandrel.service.queue;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class EnqueuedUrls implements Serializable {

	private static final long serialVersionUID = 7737480486189912755L;

	private final long spiderId;

	private final List<String> urls;

	public EnqueuedUrls(long spiderId, List<String> urls) {
		super();
		this.spiderId = spiderId;
		this.urls = urls;
	}
}
