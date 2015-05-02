package io.mandrel.gateway.impl;

import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.gateway.Document;
import io.mandrel.gateway.DocumentStore;

import java.util.List;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;

@Data
public class CassandraDocumentStore implements DocumentStore {

	private static final long serialVersionUID = 6396577303162229457L;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private transient HazelcastInstance hazelcastInstance;

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
