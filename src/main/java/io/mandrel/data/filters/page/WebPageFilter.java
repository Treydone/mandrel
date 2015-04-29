package io.mandrel.data.filters.page;

import io.mandrel.http.WebPage;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = LargeFilter.class, name = "large"), @Type(value = BooleanWebPageFilters.AndFilter.class, name = "and"),
		@Type(value = BooleanWebPageFilters.OrFilter.class, name = "or"), @Type(value = BooleanWebPageFilters.NotFilter.class, name = "not"),
		@Type(value = BooleanWebPageFilters.TrueFilter.class, name = "true"), @Type(value = BooleanWebPageFilters.FalseFilter.class, name = "false") })
@Data
public abstract class WebPageFilter implements Serializable {

	private static final long serialVersionUID = -2594414302045717456L;

	public abstract boolean isValid(WebPage webPage);
}
