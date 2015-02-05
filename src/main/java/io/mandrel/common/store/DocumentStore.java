package io.mandrel.common.store;

import io.mandrel.common.content.WebPageExtractor;
import io.mandrel.common.store.impl.CassandraDocumentStore;
import io.mandrel.common.store.impl.InternalDocumentStore;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = InternalDocumentStore.class, name = ""),
		@Type(value = InternalDocumentStore.class, name = "internal"),
		@Type(value = InternalDocumentStore.class, name = "jdbc"),
		@Type(value = CassandraDocumentStore.class, name = "cassandra") })
public interface DocumentStore {

	void init(WebPageExtractor webPageExtractor);

	boolean check();

	void save(Document doc);

	void save(List<Document> data);

}
