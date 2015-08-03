package io.mandrel;

import io.mandrel.common.querydsl.DslParser;
import io.mandrel.data.filters.link.LinkFilter;
import io.mandrel.data.filters.link.StartWithFilter;
import io.mandrel.data.filters.link.UrlPatternFilter;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DslParserTest {

	// "startwith(value:'qsd') or pattern(pattern:'ertertert') and (startwith)     and (startwith(value:'pouet') or startwith(value:'bracket'))"

	@Test
	public void start_simple() {
		LinkFilter filter = DslParser.parseLinkFilter("start_with(value:'ertertert123')");
		Assertions.assertThat(filter).isInstanceOf(StartWithFilter.class)
				.isEqualToIgnoringGivenFields(new StartWithFilter().setValue("ertertert123"), "compiledPattern");
	}

	@Test
	public void start_with_spaces() {
		LinkFilter filter = DslParser.parseLinkFilter("start_with( value : 'ertertert123' )");
		Assertions.assertThat(filter).isInstanceOf(StartWithFilter.class)
				.isEqualToIgnoringGivenFields(new StartWithFilter().setValue("ertertert123"), "compiledPattern");
	}

	@Test
	public void pattern_simple() {
		LinkFilter filter = DslParser.parseLinkFilter("pattern(value:'ertertert123')");
		Assertions.assertThat(filter).isInstanceOf(UrlPatternFilter.class)
				.isEqualToIgnoringGivenFields(new UrlPatternFilter().setValue("ertertert123"), "compiledPattern");
	}
	
	@Test
	public void pattern_with_spaces() {
		LinkFilter filter = DslParser.parseLinkFilter("pattern( value    : 'ertertert123'  )");
		Assertions.assertThat(filter).isInstanceOf(UrlPatternFilter.class)
				.isEqualToIgnoringGivenFields(new UrlPatternFilter().setValue("ertertert123"), "compiledPattern");
	}

	@Test
	public void pattern_default() {
		LinkFilter filter = DslParser.parseLinkFilter("pattern('ertertert123')");
		Assertions.assertThat(filter).isInstanceOf(UrlPatternFilter.class)
				.isEqualToIgnoringGivenFields(new UrlPatternFilter().setValue("ertertert123"), "compiledPattern");
	}
}
