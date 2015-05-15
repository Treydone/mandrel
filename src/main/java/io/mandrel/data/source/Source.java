package io.mandrel.data.source;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hazelcast.core.HazelcastInstance;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = JmsSource.class, name = "jms"), @Type(value = FixedSource.class, name = "fixed"),
		@Type(value = RobotsTxtSource.class, name = "sitemaps"), @Type(value = CsvSource.class, name = "csv"), @Type(value = JdbcSource.class, name = "jdbc") })
@Data
@Accessors(chain = true)
public abstract class Source implements Serializable {

	private static final long serialVersionUID = 7468260753688101634L;

	@JsonProperty("name")
	private String name;

	@Getter(onMethod = @__(@JsonIgnore))
	private transient HazelcastInstance instance;

	public abstract void register(EntryListener listener);

	abstract public boolean check();

	public boolean singleton() {
		return true;
	}

	public void init(Map<String, Object> properties) {

	}

}
