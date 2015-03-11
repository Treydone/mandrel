package io.mandrel.common.store;

import io.mandrel.common.content.WebPageExtractor;
import io.mandrel.common.health.Checkable;
import io.mandrel.common.store.impl.CassandraDocumentStore;
import io.mandrel.common.store.impl.InternalDocumentStore;
import io.mandrel.common.store.impl.JdbcDocumentStore;

import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = InternalDocumentStore.class, name = "internal"), @Type(value = JdbcDocumentStore.class, name = "jdbc"),
		@Type(value = CassandraDocumentStore.class, name = "cassandra") })
public interface DocumentStore extends Checkable {

	void init(WebPageExtractor webPageExtractor);

	void save(Document doc);

	void save(List<Document> data);

	Stream<Document> all();

}
