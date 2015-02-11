package io.mandrel.common.source;

import java.util.Map;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = JmsSource.class, name = "jms"),
		@Type(value = FixedSource.class, name = "fixed"),
		@Type(value = CsvSource.class, name = "csv"),
		@Type(value = JdbcSource.class, name = "jdbc") })
@Data
public abstract class Source {

	private String name;

	public abstract void register(EntryListener listener);

	abstract public boolean check();

	public boolean singleton() {
		return true;
	}

	public void init(Map<String, Object> properties) {

	}

}
