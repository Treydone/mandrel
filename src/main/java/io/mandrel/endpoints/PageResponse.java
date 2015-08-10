package io.mandrel.endpoints;

import io.mandrel.gateway.Document;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PageResponse {

	private int draw;
	private long recordsTotal;
	private long recordsFiltered;

	private List<Item> data;

	public static <U extends Collection<Document>> PageResponse of(U items) {
		return new PageResponse().setData(items.stream().map(Item::of).collect(Collectors.toList()));
	}
}
