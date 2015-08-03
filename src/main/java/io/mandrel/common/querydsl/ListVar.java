package io.mandrel.common.querydsl;

import java.util.ArrayList;
import java.util.List;

import org.parboiled.support.Var;

public class ListVar extends Var<List<String>> {

	public ListVar() {
		this(new ArrayList<>());
	}

	public ListVar(List<String> value) {
		super(value);
	}

	public boolean isEmpty() {
		return get() == null || get().size() == 0;
	}

	public boolean add(String text) {
		if (get() == null) {
			List<String> tmp = new ArrayList<String>();
			tmp.add(text);
			return set(tmp);
		}
		get().add(text);
		return true;
	}

}
