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

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;

@Data
public class InternalStore implements WebPageStore, PageMetadataStore {

	private static final long serialVersionUID = -775049235484042261L;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private transient HazelcastInstance hazelcastInstance;

	public InternalStore() {
	}

	public boolean check() {
		return true;
	}

	public void init(Map<String, Object> properties) {

	}

	public void addPage(long spiderId, String url, WebPage webPage) {
		hazelcastInstance.getMap("pagestore-" + spiderId).set(url, webPage);
	}

	public void addMetadata(long spiderId, String url, Metadata metadata) {
		hazelcastInstance.getMap("pagemetastore-" + spiderId).set(url, metadata);
	}

	public Set<String> filter(long spiderId, Set<Link> outlinks, Politeness politeness) {

		if (outlinks == null) {
			return null;
		}

		Set<String> uris = outlinks.stream().filter(ol -> ol != null && StringUtils.isNotBlank(ol.getUri())).map(ol -> ol.getUri()).collect(Collectors.toSet());
		Map<String, Metadata> all = hazelcastInstance.<String, Metadata> getMap("pagemetastore-" + spiderId).getAll(uris);

		int recrawlAfterSeconds = politeness.getRecrawlAfterSeconds();
		LocalDateTime now = LocalDateTime.now();
		return outlinks.stream().filter(outlink -> {
			Metadata entry = all.get(outlink.getUri());

			if (entry == null) {
				return true;
			}

			if (recrawlAfterSeconds > 0 && now.minusSeconds(recrawlAfterSeconds).isAfter(entry.getLastCrawlDate())) {
				return true;
			}

			return false;
		}).map(l -> l.getUri()).collect(Collectors.toSet());
	}

	public Stream<WebPage> all(long spiderId) {
		return hazelcastInstance.<String, WebPage> getMap("pagestore-" + spiderId).values().stream();
	}

	public void deleteAllFor(long spiderId) {
		hazelcastInstance.getMap("pagestore-" + spiderId).destroy();
		hazelcastInstance.getMap("pagemetastore-" + spiderId).destroy();
	}

}
