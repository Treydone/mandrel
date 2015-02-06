package io.mandrel.service.spider;

import static org.junit.Assert.assertEquals;
import io.mandrel.common.source.SeedsSourceTest.LocalConfiguration;
import io.mandrel.config.BindConfiguration;
import io.mandrel.service.spider.Spider.Client;
import io.mandrel.service.spider.Spider.Stores;

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
	public void spider() throws IOException {

		Spider spider = new Spider();

		String json = objectMapper.writeValueAsString(spider);
		System.err.println(json);
		Spider read = objectMapper.readValue(json, Spider.class);
		assertEquals(spider, read);
	}
}