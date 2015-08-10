package io.mandrel.endpoints;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

@Data
public class Item<T> {

	private String DT_RowId;
	private String DT_RowClass;

	@JsonUnwrapped
	private final T data;

	public static <T> Item<T> of(T data) {
		return new Item<T>(data);
	}
}
