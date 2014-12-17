package io.mandrel.common.filters;

import lombok.Data;
import io.mandrel.common.WebPage;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = ReferencedFilter.class, name = "ref"),
		@Type(value = UrlPatternFilter.class, name = "pattern") })
@Data
public abstract class WebPageFilter {

	abstract boolean isValid(WebPage webPage);
}
