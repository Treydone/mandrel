package io.mandrel.common.data;

import io.mandrel.data.filters.link.LinkFilter;
import io.mandrel.data.filters.page.WebPageFilter;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Filters {

	private List<WebPageFilter> forPages = new ArrayList<>();
	private List<LinkFilter> forLinks = new ArrayList<>();
}
