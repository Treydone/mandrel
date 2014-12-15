package io.mandrel.common.source;

public class JMSSource implements Source {

	public void register(EntryListener listener) {

	}

	public String getType() {
		return "jms";
	}
}
