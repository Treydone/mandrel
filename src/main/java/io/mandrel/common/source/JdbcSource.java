package io.mandrel.common.source;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class JdbcSource extends Source {

	private String query;
	private String url;

	public void register(EntryListener listener) {

	}

	public boolean check() {
		return true;
	}
}
