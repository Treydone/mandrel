package io.mandrel.common.filters;

import io.mandrel.common.WebPage;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = LargeFilter.class, name = "large"),
		@Type(value = BooleanFilters.AndFilter.class, name = "and"),
		@Type(value = BooleanFilters.OrFilter.class, name = "or"),
		@Type(value = BooleanFilters.NotFilter.class, name = "not"),
		@Type(value = BooleanFilters.TrueFilter.class, name = "true"),
		@Type(value = BooleanFilters.FalseFilter.class, name = "false"),
		@Type(value = UrlPatternFilter.class, name = "pattern") })
@Data
public abstract class WebPageFilter {

	abstract boolean isValid(WebPage webPage);
}
