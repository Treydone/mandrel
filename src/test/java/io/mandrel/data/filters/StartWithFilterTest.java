package io.mandrel.data.filters;

import io.mandrel.data.filters.link.StartWithFilter;
import io.mandrel.data.spider.Link;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class StartWithFilterTest {

	private StartWithFilter filter = new StartWithFilter().setPattern("http://localhost/1");

	@Test
	public void no_link() {
		Assertions.assertThat(filter.isValid(new Link())).isFalse();
	}

	@Test
	public void link_start_with_exact() {
		Assertions.assertThat(filter.isValid(new Link().setUri("http://localhost/1"))).isTrue();
	}

	@Test
	public void link_start_with_partially() {
		Assertions.assertThat(filter.isValid(new Link().setUri("http://localhost/1/other"))).isTrue();
	}

	@Test
	public void link_not_start_with() {
		Assertions.assertThat(filter.isValid(new Link().setUri("http://test/1"))).isFalse();
	}
}
