package io.mandrel.data.filters;

import io.mandrel.data.filters.link.UrlPatternFilter;
import io.mandrel.data.spider.Link;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class UrlPatternFitlerTest {

	private UrlPatternFilter filter = UrlPatternFilter.STATIC;

	@Test
	public void no_link() {
		Assertions.assertThat(filter.isValid(new Link())).isFalse();
	}

	@Test
	public void link_simple() {
		Assertions.assertThat(filter.isValid(new Link().setUri("http://localhost/1"))).isTrue();
	}

	@Test
	public void link_static() {
		Assertions.assertThat(filter.isValid(new Link().setUri("http://localhost/1.jpg"))).isFalse();
	}
}
