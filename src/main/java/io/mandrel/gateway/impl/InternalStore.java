package io.mandrel.gateway.impl;

import io.mandrel.common.data.Politeness;
import io.mandrel.gateway.PageMetadataStore;
import io.mandrel.gateway.WebPageStore;
import io.mandrel.http.Metadata;
import io.mandrel.http.WebPage;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void init(Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	public void addPage(WebPage webPage) {
		instance.getMap("pagestore").set(webPage.getUrl().toString(), webPage);
	}

	@Override
	public void addMetadata(WebPage webPage) {
		instance.getMap("pagemetastore").set(webPage.getUrl().toString(), webPage.getMetadata());
	}

	@Override
	public Set<String> filter(Set<String> outlinks, Politeness politeness) {

		int recrawlAfterSeconds = politeness.getRecrawlAfterSeconds();

		Map<Object, Object> all = instance.getMap("pagemetastore").getAll((Set) outlinks);

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
}
