package io.mandrel.gateway.impl;

import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.gateway.Document;
import io.mandrel.gateway.DocumentStore;

import java.util.List;
import java.util.stream.Stream;

public class CassandraDocumentStore implements DocumentStore {

	@Override
	public void save(long spiderId, Document data) {
		// TODO
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
	public Stream<Document> all(long spiderId) {
		// TODO Auto-generated method stub
		return null;
	}
}
