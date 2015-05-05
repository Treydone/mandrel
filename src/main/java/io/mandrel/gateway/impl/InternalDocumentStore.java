package io.mandrel.gateway.impl;

import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.gateway.Document;
import io.mandrel.gateway.DocumentStore;

import java.util.Collection;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.util.IterationType;

@Data
public class InternalDocumentStore implements DocumentStore {

	private static final long serialVersionUID = -2445958974306201476L;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private transient HazelcastInstance hazelcastInstance;

	@Override
	public void save(long spiderId, Document data) {
		// TODO
		// instance.getMap("store").set(null, data);
	}

	@Override
	public void save(long spiderId, List<Document> data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(WebPageExtractor webPageExtractor) {

	}

	@Override
	public boolean check() {
		return false;
	}

	@Override
	public void deleteAllFor(long spiderId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void byPages(long spiderId, int pageSize, Callback callback) {
		PagingPredicate predicate = new PagingPredicate(pageSize);
		predicate.setIterationType(IterationType.VALUE);

		boolean loop = true;
		while (loop) {
			Collection<Document> values = hazelcastInstance.<String, Document> getMap("documentstore-" + spiderId).values(predicate);
			loop = callback.on(values);
			predicate.nextPage();
		}
	}
}
