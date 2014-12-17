package io.mandrel.common.source;

import java.util.List;
import java.util.Map;

public class SeedsSource implements Source {

	private List<String> seeds;

	public SeedsSource(List<String> seeds) {
		this.seeds = seeds;
	}

	public void register(EntryListener listener) {
		for (String seed : seeds) {
			listener.onItem(seed);
		}
	}

	public void init(Map<String, Object> properties) {

	}

	public String getType() {
		return "seed";
	}
}
