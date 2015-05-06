package io.mandrel.gateway.impl;

import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.gateway.Document;
import io.mandrel.gateway.DocumentStore;

import java.util.Collection;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.util.IterationType;

@Data
public class InternalDocumentStore implements DocumentStore {

	private static final long serialVersionUID = -2445958974306201476L;

	@JsonIgnore
	private WebPageExtractor extractor;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private transient HazelcastInstance hazelcastInstance;

	@Override
	public void save(long spiderId, Document data) {
		if (data != null) {
			hazelcastInstance.getMap("documentstore-" + spiderId + "-" + extractor.getName()).put(getKey(spiderId, data), data);
		}
	}

	@Override
	public void save(long spiderId, List<Document> data) {
		if (data != null) {
			data.forEach(el -> {
				hazelcastInstance.getMap("documentstore-" + spiderId + "-" + extractor.getName()).put(getKey(spiderId, el), el);
			});
		}
	}

	@Override
	public void init(WebPageExtractor webPageExtractor) {
		this.extractor = webPageExtractor;
	}

	@Override
	public boolean check() {
		try {
			hazelcastInstance.getCluster();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void deleteAllFor(long spiderId) {
		hazelcastInstance.getMap("documentstore-" + spiderId + "-" + extractor.getName()).clear();
	}

	@Override
	public void byPages(long spiderId, int pageSize, Callback callback) {
		PagingPredicate predicate = new PagingPredicate(pageSize);
		predicate.setIterationType(IterationType.VALUE);

		boolean loop = true;
		while (loop) {
			Collection<Document> values = hazelcastInstance.<String, Document> getMap("documentstore-" + spiderId + "-" + extractor.getName())
					.values(predicate);
			loop = callback.on(values);
			predicate.nextPage();
		}
	}

	public String getKey(long spiderId, Document data) {
		String key = null;
		if (StringUtils.isNotBlank(extractor.getKeyField())) {
			List<? extends Object> values = data.get(extractor.getKeyField());
			if (CollectionUtils.isNotEmpty(values)) {
				key = values.get(0).toString();
			}
		}
		if (key == null) {
			key = String.valueOf(hazelcastInstance.getIdGenerator("documentstore-" + spiderId + "-" + extractor.getName()).newId());
		}
		return key;
	}
}
