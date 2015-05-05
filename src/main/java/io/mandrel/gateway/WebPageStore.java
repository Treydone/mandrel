package io.mandrel.gateway;

import io.mandrel.gateway.impl.InternalStore;
import io.mandrel.http.WebPage;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hazelcast.core.HazelcastInstanceAware;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = InternalStore.class, name = "internal") })
public interface WebPageStore extends Checkable, Serializable, HazelcastInstanceAware {

	void addPage(long spiderId, String url, WebPage webPage);

	WebPage getPage(long spiderId, String url);

	void deleteAllFor(long spiderId);

	void init(Map<String, Object> properties);

	// Stream<WebPage> all(long spiderId);

	@FunctionalInterface
	public static interface Callback {
		boolean on(Collection<WebPage> elements);
	}

	void byPages(long spiderId, int pageSize, Callback callback);
}
