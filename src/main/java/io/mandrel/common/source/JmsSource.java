package io.mandrel.common.source;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class JmsSource extends Source {

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
