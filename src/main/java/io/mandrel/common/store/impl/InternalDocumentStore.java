package io.mandrel.common.store.impl;

import io.mandrel.common.content.WebPageExtractor;
import io.mandrel.common.store.Document;
import io.mandrel.common.store.DocumentStore;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;

public class InternalDocumentStore implements DocumentStore {

	@JsonIgnore
	private HazelcastInstance instance;

	@Override
	public void save(Document data) {
		// TODO
		// instance.getMap("store").set(null, data);
	}

	@Override
	public void save(List<Document> data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(WebPageExtractor webPageExtractor) {

	}

	@Override
	public boolean check() {
		return false;
	}
}
