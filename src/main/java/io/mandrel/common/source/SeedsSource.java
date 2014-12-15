package io.mandrel.common.source;

import java.util.List;

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

	public String getType() {
		return "seed";
	}
}
