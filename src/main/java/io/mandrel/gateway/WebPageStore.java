package io.mandrel.gateway;

import io.mandrel.gateway.impl.InternalStore;
import io.mandrel.http.WebPage;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = InternalStore.class, name = "internal") })
public interface WebPageStore extends Checkable, Serializable {

	void addPage(long spiderId, String url, WebPage webPage);

	void deleteAllFor(long spiderId);

	void init(Map<String, Object> properties);

	Stream<WebPage> all(long spiderId);
}
