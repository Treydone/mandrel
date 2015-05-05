package io.mandrel.gateway;

import io.mandrel.common.data.Politeness;
import io.mandrel.data.spider.Link;
import io.mandrel.gateway.impl.CassandraStore;
import io.mandrel.gateway.impl.InternalStore;
import io.mandrel.gateway.impl.JdbcStore;
import io.mandrel.http.Metadata;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hazelcast.core.HazelcastInstanceAware;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = InternalStore.class, name = "internal"), @Type(value = JdbcStore.class, name = "jdbc"),
		@Type(value = CassandraStore.class, name = "cassandra") })
public interface PageMetadataStore extends Checkable, Serializable, HazelcastInstanceAware {

	void addMetadata(long spiderId, String url, Metadata metadata);

	Metadata getMetadata(long spiderId, String url);

	void init(Map<String, Object> properties);

	Set<String> filter(long spiderId, Set<Link> outlinks, Politeness politeness);
}
