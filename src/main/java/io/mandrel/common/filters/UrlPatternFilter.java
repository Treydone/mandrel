package io.mandrel.common.filters;

import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.mandrel.common.WebPage;

@Data
@EqualsAndHashCode(callSuper = false, exclude = "compiledPattern")
public class UrlPatternFilter extends WebPageFilter {

	@JsonIgnore
	private Pattern compiledPattern;
	private String pattern;

	public boolean isValid(WebPage webPage) {
		return compiledPattern.matcher(webPage.getUrl().toString()).matches();
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
		compiledPattern = Pattern.compile(pattern);
	}
}
