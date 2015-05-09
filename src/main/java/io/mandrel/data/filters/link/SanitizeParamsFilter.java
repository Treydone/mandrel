package io.mandrel.data.filters.link;

import io.mandrel.data.spider.Link;

import java.util.List;
import java.util.regex.Pattern;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SanitizeParamsFilter extends LinkFilter {

	private static final long serialVersionUID = -8284466714206360251L;

	private List<String> exclusions;

	private final static Pattern QUERY = Pattern.compile("&");

	private final static Pattern PARAMS = Pattern.compile("=");

	public boolean isValid(Link link) {
		if (link != null && StringUtils.isNotBlank(link.getUri())) {
			int pos = link.getUri().indexOf('?');
			if (pos > -1) {
				String uriWithoutParams = link.getUri().substring(0, pos);

				if (CollectionUtils.isNotEmpty(exclusions)) {
					String query = link.getUri().substring(pos + 1, link.getUri().length());

					if (StringUtils.isNotBlank(query)) {
						String[] paramPairs = QUERY.split(query);

						MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
						if (paramPairs != null) {
							for (String pair : paramPairs) {
								String[] paramValue = PARAMS.split(pair);
								if (exclusions.contains(paramValue[0])) {
									params.add(paramValue[0], paramValue.length > 1 ? paramValue[1] : null);
								}
							}
						}

						StringBuilder builder = new StringBuilder();
						params.entrySet().forEach(entry -> {
							entry.getValue().forEach(value -> {
								builder.append(entry.getKey());
								if (StringUtils.isNotBlank(value)) {
									builder.append("=").append(value);
								}
								builder.append("&");
							});
						});
						if (builder.length() > 0 && builder.charAt(builder.length() - 1) == '&') {
							builder.deleteCharAt(builder.length() - 1);
						}
						if (builder.length() > 0) {
							link.setUri(uriWithoutParams + "?" + builder.toString());
						} else {
							link.setUri(uriWithoutParams);
						}
					} else {
						link.setUri(uriWithoutParams);
					}
				} else {
					link.setUri(uriWithoutParams);
				}
			}
		}
		return true;
	}
}
