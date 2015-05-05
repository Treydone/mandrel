package io.mandrel.gateway;

import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.gateway.impl.CassandraDocumentStore;
import io.mandrel.gateway.impl.InternalDocumentStore;
import io.mandrel.gateway.impl.JdbcDocumentStore;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hazelcast.core.HazelcastInstanceAware;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = InternalDocumentStore.class, name = "internal"), @Type(value = JdbcDocumentStore.class, name = "jdbc"),
		@Type(value = CassandraDocumentStore.class, name = "cassandra") })
public interface DocumentStore extends Checkable, Serializable, HazelcastInstanceAware {

	void init(WebPageExtractor webPageExtractor);

	void save(long spiderId, Document doc);

	void save(long spiderId, List<Document> data);

	void deleteAllFor(long spiderId);

	// Stream<Document> all(long spiderId);

	@FunctionalInterface
	public static interface Callback {
		boolean on(Collection<Document> elements);
	}

	void byPages(long spiderId, int pageSize, Callback callback);

}
