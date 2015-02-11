package io.mandrel.common.source;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FixedSource extends Source {

	private List<String> seeds;

	public void register(EntryListener listener) {
		for (String seed : seeds) {
			listener.onItem(seed);
		}
	}

	public boolean check() {
		return true;
	}
}
