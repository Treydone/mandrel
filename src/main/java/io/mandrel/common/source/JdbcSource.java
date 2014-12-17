package io.mandrel.common.source;

import java.util.Map;

public class JdbcSource implements Source {

	// private final JdbcOperations jdbcOperations;
	//
	// private final String query;
	//
	// public JdbcSource(JdbcOperations jdbcOperations, String query) {
	// this.jdbcOperations = jdbcOperations;
	// this.query = query;
	// }

	public void init(Map<String, Object> properties) {

	}

	public void register(EntryListener listener) {
	}

	public String getType() {
		return "jdbc";
	}
}
