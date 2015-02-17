package io.mandrel.common.filters;

import io.mandrel.common.WebPage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class StaticsFilter extends WebPageFilter {

	private static final long serialVersionUID = -1570180634804031306L;

	public boolean isValid(WebPage webPage) {
		webPage.getMetadata().getHeaders().getFirstValue("Content-type");
		// TODO
		return true;
	}
}
