package io.mandrel.data.filters;

import io.mandrel.data.filters.link.SanitizeParamsFilter;
import io.mandrel.data.spider.Link;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class SanitizeParamsFilterTest {

	private SanitizeParamsFilter filter = new SanitizeParamsFilter();

	@Test
	public void no_link() {
		Assertions.assertThat(filter.isValid(new Link())).isTrue();
	}

	@Test
	public void link_without_params() {
		Link link = new Link().setUri("http://localhost/1");
		Assertions.assertThat(filter.isValid(link)).isTrue();
		Assertions.assertThat(link.getUri()).isEqualTo("http://localhost/1");
	}

	@Test
	public void link_with_params() {
		Link link = new Link().setUri("http://localhost/1?foo=test&foo2=test2");
		Assertions.assertThat(filter.isValid(link)).isTrue();
		Assertions.assertThat(link.getUri()).isEqualTo("http://localhost/1");
	}
	
	@Test
	public void link_with_ancor() {
		Link link = new Link().setUri("http://localhost/1#foo");
		Assertions.assertThat(filter.isValid(link)).isTrue();
		Assertions.assertThat(link.getUri()).isEqualTo("http://localhost/1");
	}
}
