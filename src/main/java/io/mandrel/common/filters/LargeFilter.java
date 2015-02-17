package io.mandrel.common.filters;

import io.mandrel.common.WebPage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LargeFilter extends WebPageFilter {

	private static final long serialVersionUID = -5624619977747831604L;

	public boolean isValid(WebPage webPage) {
		webPage.getMetadata().getHeaders().getFirstValue("Content-size");
		// TODO
		return true;
	}
}
