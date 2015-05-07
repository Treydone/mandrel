package io.mandrel.data.filters.link;

import io.mandrel.data.spider.Link;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = BooleanLinkFilters.AndFilter.class, name = "and"), @Type(value = BooleanLinkFilters.OrFilter.class, name = "or"),
		@Type(value = BooleanLinkFilters.NotFilter.class, name = "not"), @Type(value = BooleanLinkFilters.TrueFilter.class, name = "true"),
		@Type(value = BooleanLinkFilters.FalseFilter.class, name = "false"), @Type(value = AllowedForDomainsFilter.class, name = "allowed_for_domains"),
		@Type(value = SkipAncorFilter.class, name = "skip_ancor"), @Type(value = UrlPatternFilter.class, name = "pattern"),
		@Type(value = StartWithFilter.class, name = "start_with"), @Type(value = SanitizeParamsFilter.class, name = "sanitize_params") })
@Data
public abstract class LinkFilter implements Serializable {

	private static final long serialVersionUID = 4415317526564869848L;

	public abstract boolean isValid(Link link);
}
