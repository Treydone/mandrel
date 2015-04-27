package io.mandrel.data.spider;

import static org.junit.Assert.assertEquals;
import io.mandrel.common.data.Client;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Stores;
import io.mandrel.config.BindConfiguration;
import io.mandrel.data.filters.UrlPatternFilter;
import io.mandrel.data.filters.WebPageFilter;
import io.mandrel.data.spider.SpiderService;
import io.mandrel.data.spider.SpiderServiceTest.LocalConfiguration;

import java.io.IOException;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.Errors;

import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = LocalConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SpiderServiceTest {

	@Configuration
	@Import(BindConfiguration.class)
	public static class LocalConfiguration {

	}

	@Inject
	private ObjectMapper objectMapper;

	@Test
	public void client() throws IOException {

		Client client = new Client();

		String json = objectMapper.writeValueAsString(client);
		System.err.println(json);
		Client read = objectMapper.readValue(json, Client.class);
		assertEquals(client, read);
	}

	@Test
	public void stores() throws IOException {

		Stores stores = new Stores();

		String json = objectMapper.writeValueAsString(stores);
		System.err.println(json);
		Stores read = objectMapper.readValue(json, Stores.class);
		assertEquals(stores, read);
	}

	@Test
	public void webPageFilters() throws IOException {

		UrlPatternFilter filter = new UrlPatternFilter();
		filter.setPattern(".*");

		String json = objectMapper.writeValueAsString(filter);
		System.err.println(json);
		WebPageFilter read = objectMapper.readValue(json, WebPageFilter.class);
		assertEquals(filter, read);
	}

	@Test
	public void spider() throws IOException {

		Spider spider = new Spider();

		String json = objectMapper.writeValueAsString(spider);
		System.err.println(json);
		Spider read = objectMapper.readValue(json, Spider.class);
		assertEquals(spider, read);
	}

	@Test
	public void validate() throws IOException {

		Spider spider = new Spider();

		SpiderService spiderService = new SpiderService(null, null, null, null);
		Errors errors = spiderService.validate(spider);

		System.err.println(errors);

	}
}