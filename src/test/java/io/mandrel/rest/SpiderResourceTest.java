package io.mandrel.rest;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class SpiderResourceTest {

	private RestTemplate restTemplate = new RestTemplate();

	@Test
	public void all() {

		// ResponseEntity<Json> response =
		// restTemplate.getForEntity("http://localhost:8080/rest/spider/all",
		// Map.class);

		// response.getBody().get("");

		// SpiderResource api =
		// JAXRSClientFactory.create("http://localhost:8080/rest",
		// SpiderResource.class, Arrays.asList(new JacksonJsonProvider()));
		// List<Spider> spiders = api.all();
		// System.err.println(spiders);
	}
}
