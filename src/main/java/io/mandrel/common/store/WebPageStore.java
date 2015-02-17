package io.mandrel.common.store;

import io.mandrel.common.WebPage;
import io.mandrel.common.health.Checkable;
import io.mandrel.common.store.impl.InternalStore;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = InternalStore.class, name = "internal") })
public interface WebPageStore extends Checkable, Serializable {

	void addPage(WebPage webPage);

	void init(Map<String, Object> properties);
}
