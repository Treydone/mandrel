package io.mandrel.data.filters;

import io.mandrel.http.WebPage;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AllowedForDomainsFilter extends WebPageFilter {

	private static final long serialVersionUID = -5195589618123470396L;

	private List<String> domains;

	public boolean isValid(WebPage webPage) {
		if (domains != null) {
			return domains.stream().anyMatch(d -> d.endsWith(webPage.getUrl().getHost()));
		}
		return false;
	}
}
