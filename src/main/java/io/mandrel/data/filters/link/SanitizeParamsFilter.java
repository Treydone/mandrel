package io.mandrel.data.filters.link;

import io.mandrel.data.spider.Link;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SanitizeParamsFilter extends LinkFilter {

	private static final long serialVersionUID = -8284466714206360251L;

	public boolean isValid(Link link) {
		if (link != null && StringUtils.isNotBlank(link.getUri())) {
			int pos = link.getUri().indexOf('?');
			if (pos > -1) {
				link.setUri(link.getUri().substring(0, pos));
			}
			
			pos = link.getUri().indexOf('#');
			if (pos > -1) {
				link.setUri(link.getUri().substring(0, pos));
			}
		}
		return true;
	}
}
