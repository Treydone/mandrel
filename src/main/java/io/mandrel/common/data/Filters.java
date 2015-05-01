package io.mandrel.common.data;

import io.mandrel.data.filters.link.LinkFilter;
import io.mandrel.data.filters.page.WebPageFilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Filters implements Serializable {

	private static final long serialVersionUID = -482772147190412226L;

	private List<WebPageFilter> forPages = new ArrayList<>();
	private List<LinkFilter> forLinks = new ArrayList<>();
}
