package io.mandrel.data.filters.link;

import io.mandrel.data.spider.Link;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class SkipAncorFilter extends LinkFilter {

	private static final long serialVersionUID = -5195589618123470396L;

	public boolean isValid(Link link) {
		return link != null && StringUtils.isNotBlank(link.getUri()) && (!link.getUri().contains("#") || link.getUri().contains("#/"));
	}
}
