package io.mandrel.data.filters.page;

import io.mandrel.http.WebPage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LargeFilter extends WebPageFilter {

	private static final long serialVersionUID = -5624619977747831604L;

	public boolean isValid(WebPage webPage) {
		// webPage.getMetadata().getHeaders().getFirstValue("Content-Length");
		// TODO
		return true;
	}
}
