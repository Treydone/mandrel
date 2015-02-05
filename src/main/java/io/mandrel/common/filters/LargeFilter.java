package io.mandrel.common.filters;

import io.mandrel.common.WebPage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LargeFilter extends WebPageFilter {

	public boolean isValid(WebPage webPage) {
		webPage.getHeaders().getFirstValue("Content-size");
		// TODO
		return true;
	}
}
