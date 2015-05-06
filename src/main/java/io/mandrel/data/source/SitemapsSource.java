package io.mandrel.data.source;

import java.util.ArrayList;
import java.util.List;

public class SitemapsSource extends Source {

	private static final long serialVersionUID = 7030874477659153772L;

	@Override
	public void register(EntryListener listener) {
		List<String> urls = new ArrayList<>();

		for (String seed : urls) {
			listener.onItem(seed);
		}
	}

	@Override
	public boolean check() {
		return true;
	}
}
