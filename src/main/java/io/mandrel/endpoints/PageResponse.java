package io.mandrel.endpoints;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PageResponse<T> {

	private int draw;
	private long recordsTotal;
	private long recordsFiltered;

	private List<Item<T>> data;

	public static <T, U extends Collection<T>> PageResponse<T> of(U items) {
		return new PageResponse<T>().setData(items.stream().map(Item::of).collect(Collectors.toList()));
	}
}
