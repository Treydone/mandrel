package io.mandrel.data.filters.link;

import io.mandrel.data.spider.Link;

import java.util.regex.Pattern;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@EqualsAndHashCode(callSuper = false, exclude = "compiledPattern")
public class UrlPatternFilter extends LinkFilter {

	private static final long serialVersionUID = -5195589618123470396L;

	@JsonIgnore
	private Pattern compiledPattern;
	private String pattern;

	public boolean isValid(Link link) {
		return compiledPattern.matcher(link.getUri()).matches();
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
		compiledPattern = Pattern.compile(pattern);
	}
}
