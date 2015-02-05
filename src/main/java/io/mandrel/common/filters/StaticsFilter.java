package io.mandrel.common.filters;

import io.mandrel.common.WebPage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class StaticsFilter extends WebPageFilter {

	public boolean isValid(WebPage webPage) {
		webPage.getHeaders().getFirstValue("Content-type");
		// TODO
		return true;
	}
}
