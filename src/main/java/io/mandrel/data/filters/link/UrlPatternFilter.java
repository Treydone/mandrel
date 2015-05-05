package io.mandrel.data.filters.link;

import io.mandrel.data.spider.Link;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@EqualsAndHashCode(callSuper = false, exclude = "compiledPattern")
@Accessors(chain = true)
public class UrlPatternFilter extends LinkFilter {

	private static final long serialVersionUID = -5195589618123470396L;

	public static UrlPatternFilter STATIC = new UrlPatternFilter()
			.setPattern(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");

	@JsonIgnore
	private Pattern compiledPattern;
	private String pattern;

	public boolean isValid(Link link) {
		return link != null && StringUtils.isNotBlank(link.getUri()) && !compiledPattern.matcher(link.getUri()).matches();
	}

	public UrlPatternFilter setPattern(String pattern) {
		this.pattern = pattern;
		compiledPattern = Pattern.compile(pattern);
		return this;
	}
}
