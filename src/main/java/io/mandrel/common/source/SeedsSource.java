package io.mandrel.common.source;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class SeedsSource extends Source {

	private List<String> seeds;

	public void register(EntryListener listener) {
		for (String seed : seeds) {
			listener.onItem(seed);
		}
	}

	public void init(Map<String, Object> properties) {

	}
}
