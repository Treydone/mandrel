package io.mandrel.data.filters;

import io.mandrel.data.filters.link.UrlPatternFilter;
import io.mandrel.data.spider.Link;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class UrlPatternFitlerTest {

	private UrlPatternFilter _static = UrlPatternFilter.STATIC;

	private UrlPatternFilter _custom = new UrlPatternFilter().setPattern("http://localhost/users/(\\d+).html");

	private UrlPatternFilter _custom2 = new UrlPatternFilter().setPattern("http://localhost/users/(\\d+).html(.*)");

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

	@Test
	public void custom_link() {
		Assertions.assertThat(_custom.isValid(new Link().setUri("http://localhost/1.jpg"))).isFalse();
		Assertions.assertThat(_custom.isValid(new Link().setUri("http://localhost/users/1.jpg"))).isFalse();
		Assertions.assertThat(_custom.isValid(new Link().setUri("http://localhost/users/13455"))).isFalse();
		Assertions.assertThat(_custom.isValid(new Link().setUri("http://localhost/users/others/others"))).isFalse();
		Assertions.assertThat(_custom.isValid(new Link().setUri("http://localhost/users/1.html"))).isTrue();
		Assertions.assertThat(_custom.isValid(new Link().setUri("http://localhost/users/1.html?test"))).isFalse();

		Assertions.assertThat(_custom2.isValid(new Link().setUri("http://localhost/1.jpg"))).isFalse();
		Assertions.assertThat(_custom2.isValid(new Link().setUri("http://localhost/users/1.jpg"))).isFalse();
		Assertions.assertThat(_custom2.isValid(new Link().setUri("http://localhost/users/13455"))).isFalse();
		Assertions.assertThat(_custom2.isValid(new Link().setUri("http://localhost/users/others/others"))).isFalse();
		Assertions.assertThat(_custom2.isValid(new Link().setUri("http://localhost/users/1.html"))).isTrue();
		Assertions.assertThat(_custom2.isValid(new Link().setUri("http://localhost/users/1.html?test"))).isTrue();
	}
}
