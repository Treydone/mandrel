package io.mandrel.common.source;

import java.util.Map;

public class JMSSource implements Source {

	public void register(EntryListener listener) {

	}

	public void init(Map<String, Object> properties) {

	}

	public String getType() {
		return "jms";
	}
}
