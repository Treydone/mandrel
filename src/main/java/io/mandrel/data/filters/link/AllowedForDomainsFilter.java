package io.mandrel.data.filters.link;

import io.mandrel.data.spider.Link;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AllowedForDomainsFilter extends LinkFilter {

	private static final long serialVersionUID = -5195589618123470396L;

	private List<String> domains;

	public boolean isValid(Link link) {
		if (CollectionUtils.isNotEmpty(domains) && link != null && StringUtils.isNotBlank(link.getUri())) {
			return domains.stream().anyMatch(d -> link.getUri().contains(d));
		}
		return false;
	}
}
