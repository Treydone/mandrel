package io.mandrel.data.filters;

import io.mandrel.data.filters.link.UrlPatternFilter;
import io.mandrel.data.spider.Link;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class UrlPatternFitlerTest {

	private UrlPatternFilter _static = UrlPatternFilter.STATIC;

	@Test
	public void static_no_link() {
		Assertions.assertThat(_static.isValid(new Link())).isFalse();
	}

	@Test
	public void static_link_simple() {
		Assertions.assertThat(_static.isValid(new Link().setUri("http://localhost/1"))).isTrue();
	}

	@Test
	public void static_link_static() {
		Assertions.assertThat(_static.isValid(new Link().setUri("http://localhost/1.jpg"))).isFalse();
	}
}
