package io.mandrel.data.source;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class JmsSource extends Source {

	private static final long serialVersionUID = -1343900230187746468L;

	private String url;

	public void register(EntryListener listener) {

	}

	@Override
	public boolean singleton() {
		return false;
	}

	public boolean check() {
		return true;
	}
}
