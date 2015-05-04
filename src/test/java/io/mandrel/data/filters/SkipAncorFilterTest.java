package io.mandrel.data.filters;

import io.mandrel.data.filters.link.SkipAncorFilter;
import io.mandrel.data.spider.Link;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class SkipAncorFilterTest {

	private SkipAncorFilter filter = new SkipAncorFilter();

	@Test
	public void no_link() {
		Assertions.assertThat(filter.isValid(new Link())).isFalse();
	}

	@Test
	public void link_without_diesis() {
		Assertions.assertThat(filter.isValid(new Link().setUri("http://localhost/1"))).isTrue();
	}

	@Test
	public void link_with_diesis() {
		Assertions.assertThat(filter.isValid(new Link().setUri("http://localhost/1#ancor"))).isFalse();
	}

	@Test
	public void link_for_one_page() {
		Assertions.assertThat(filter.isValid(new Link().setUri("http://localhost/1#/page"))).isTrue();
	}
}
