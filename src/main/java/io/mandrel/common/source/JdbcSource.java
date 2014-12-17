package io.mandrel.common.source;

import java.util.Map;

import lombok.Data;

@Data
public class JdbcSource extends Source {

	private String query;
	private String url;

	public void init(Map<String, Object> properties) {

	}

	public void register(EntryListener listener) {
	}
}
