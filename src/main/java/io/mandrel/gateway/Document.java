package io.mandrel.gateway;

import java.util.HashMap;
import java.util.List;

public class Document extends HashMap<String, List<? extends Object>> {

	private static final long serialVersionUID = 2592030198302376937L;

	public Document() {
		super();
	}

	public Document(int size) {
		super(size);
	}

}
