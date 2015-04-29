package io.mandrel.data.filters.link;

import io.mandrel.data.spider.Link;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AllowedForDomainsFilter extends LinkFilter {

	private static final long serialVersionUID = -5195589618123470396L;

	private List<String> domains;

	public boolean isValid(Link link) {
		if (CollectionUtils.isNotEmpty(domains)) {
			return domains.stream().anyMatch(d -> link.getUri().contains(d));
		}
		return false;
	}
}
