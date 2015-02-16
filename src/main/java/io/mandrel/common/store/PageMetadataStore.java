package io.mandrel.common.store;

import io.mandrel.common.health.Checkable;
import io.mandrel.common.store.impl.CassandraStore;
import io.mandrel.common.store.impl.InternalStore;
import io.mandrel.common.store.impl.JdbcStore;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = InternalStore.class, name = "internal"), @Type(value = JdbcStore.class, name = "jdbc"),
		@Type(value = CassandraStore.class, name = "cassandra") })
public interface PageMetadataStore extends Checkable, Serializable {

	void init(Map<String, Object> properties);
}
