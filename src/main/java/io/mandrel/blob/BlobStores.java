package io.mandrel.blob;

import io.mandrel.common.data.Spider;

import java.util.concurrent.ConcurrentHashMap;

public class BlobStores {

	private final static ConcurrentHashMap<Long, BlobStore> stores = new ConcurrentHashMap<>();

	public static Iterable<BlobStore> list() {
		return stores.values();
	}

	public static BlobStore create(Spider spider) {
		return stores.put(spider.getId(), spider.getStores().getBlobStore());
	}
	
	public static BlobStore get(Long spiderId) {
		return stores.get(spiderId);
	}
	
	public static void remove(Long spiderId) {
		stores.remove(spiderId);
	}
}
