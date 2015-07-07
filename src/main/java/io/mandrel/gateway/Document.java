package io.mandrel.gateway;

import java.util.HashMap;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Document extends HashMap<String, List<? extends Object>> {

	private static final long serialVersionUID = 2592030198302376937L;

	private String id;

	public Document() {
		super();
	}

	public Document(int size) {
		super(size);
	}
}
