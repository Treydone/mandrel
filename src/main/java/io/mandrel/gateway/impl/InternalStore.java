package io.mandrel.gateway.impl;

import io.mandrel.common.data.Politeness;
import io.mandrel.data.spider.Link;
import io.mandrel.gateway.PageMetadataStore;
import io.mandrel.gateway.WebPageStore;
import io.mandrel.http.Metadata;
import io.mandrel.http.WebPage;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;

@Data
public class InternalStore implements WebPageStore, PageMetadataStore {

	private static final long serialVersionUID = -775049235484042261L;

	@JsonIgnore
	private HazelcastInstance instance;

	public InternalStore() {
	}

	public boolean check() {
		return true;
	}

	public void init(Map<String, Object> properties) {

	}

	public void addPage(long spiderId, String url, WebPage webPage) {
		instance.getMap("pagestore-" + spiderId).set(url, webPage);
	}

	public void addMetadata(long spiderId, String url, Metadata metadata) {
		instance.getMap("pagemetastore-" + spiderId).set(url, metadata);
	}

	public Set<Link> filter(long spiderId, Set<Link> outlinks, Politeness politeness) {

		int recrawlAfterSeconds = politeness.getRecrawlAfterSeconds();

		Map<String, Metadata> all = instance.<String, Metadata> getMap("pagemetastore-" + spiderId).getAll(
				outlinks.stream().map(ol -> ol.getUri()).collect(Collectors.toSet()));

		LocalDateTime now = LocalDateTime.now();
		return outlinks.stream().filter(outlink -> {
			Metadata entry = (Metadata) all.get(outlink);

			if (entry == null) {
				return true;
			}

			if (recrawlAfterSeconds > 0 && now.minusSeconds(recrawlAfterSeconds).isAfter(entry.getLastCrawlDate())) {
				return true;
			}

			return false;
		}).collect(Collectors.toSet());
	}

	public Stream<WebPage> all(long spiderId) {
		return instance.<String, WebPage> getMap("pagestore-" + spiderId).values().stream();
	}

	public void deleteAllFor(long spiderId) {
		instance.getMap("pagestore-" + spiderId).destroy();
		instance.getMap("pagemetastore-" + spiderId).destroy();
	}
}
