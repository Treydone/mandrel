package io.mandrel.common.filters;

import static org.junit.Assert.assertEquals;
import io.mandrel.common.filters.WebPageFiltersTest.LocalConfiguration;
import io.mandrel.config.BindConfiguration;

import java.io.IOException;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = LocalConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class WebPageFiltersTest {

	@Configuration
	@Import(BindConfiguration.class)
	public static class LocalConfiguration {

	}

	@Inject
	private ObjectMapper objectMapper;

	@Test
	public void ref() throws IOException {

		ReferencedFilter filter = new ReferencedFilter();
		filter.setRef("test");

		String json = objectMapper.writeValueAsString(filter);
		System.err.println(json);
		WebPageFilter read = objectMapper.readValue(json, WebPageFilter.class);
		assertEquals(filter, read);
	}
}
