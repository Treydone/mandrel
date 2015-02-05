package io.mandrel.service.queue;

import java.util.List;

import lombok.Data;

@Data
public class EnqueuedUrls {

	private final long spiderId;

	private final List<String> urls;

	public EnqueuedUrls(long spiderId, List<String> urls) {
		super();
		this.spiderId = spiderId;
		this.urls = urls;
	}
}
