package io.mandrel.common.store;

import java.util.HashMap;
import java.util.List;

public class Document extends HashMap<String, List<Object>> {

	public Document() {
		super();
	}

	public Document(int size) {
		super(size);
	}

}
