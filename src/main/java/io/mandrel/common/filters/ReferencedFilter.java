package io.mandrel.common.filters;

import io.mandrel.common.WebPage;
import lombok.Data;

@Data
public class ReferencedFilter extends WebPageFilter {

	private String ref;

	public boolean isValid(WebPage webPage) {
		return true;
	}
}
