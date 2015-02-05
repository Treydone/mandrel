package io.mandrel.common.source;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = JmsSource.class, name = "jms"),
		@Type(value = SeedsSource.class, name = "seed"),
		@Type(value = JdbcSource.class, name = "jdbc") })
public abstract class Source {

	abstract void register(EntryListener listener);
	
	abstract public boolean check();
	
	public void init(Map<String, Object> properties) {

	}
}
