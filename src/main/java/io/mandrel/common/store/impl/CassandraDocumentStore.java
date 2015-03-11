package io.mandrel.common.store.impl;

import io.mandrel.common.content.WebPageExtractor;
import io.mandrel.common.store.Document;
import io.mandrel.common.store.DocumentStore;

import java.util.List;
import java.util.stream.Stream;

public class CassandraDocumentStore implements DocumentStore {

	@Override
	public void save(Document data) {
		// TODO
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

	@Override
	public Stream<Document> all() {
		return null;
	}
}
