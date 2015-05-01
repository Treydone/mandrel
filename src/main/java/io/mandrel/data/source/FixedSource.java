package io.mandrel.data.source;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class FixedSource extends Source {

	private static final long serialVersionUID = -3095179267476304019L;

	private List<String> urls;

	public void register(EntryListener listener) {
		for (String seed : urls) {
			listener.onItem(seed);
		}
	}

	public boolean check() {
		return true;
	}

	public FixedSource() {
		super();
	}
}
