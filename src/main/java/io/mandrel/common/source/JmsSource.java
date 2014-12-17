package io.mandrel.common.source;

import java.util.Map;

import lombok.Data;

@Data
public class JmsSource extends Source {

	private String url;

	public void register(EntryListener listener) {

	}

	public void init(Map<String, Object> properties) {

	}
}
