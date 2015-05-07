package io.mandrel.endpoints.rest;

import io.mandrel.common.data.Spider;
import io.mandrel.data.spider.SpiderService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.CollectionUtils;

@RunWith(MockitoJUnitRunner.class)
public class SpiderResourceTest {

	@Mock
	private SpiderService spiderService;

	private MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		mockMvc = MockMvcBuilders.standaloneSetup(new SpiderResource(spiderService, null)).addFilter(new ApiOriginFilter(), "/*").build();
	}

	@Test
	public void all_empty() throws Exception {

		// Arrange
		Mockito.when(spiderService.list()).thenReturn(Stream.of());

		// Actions
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/spiders").accept(MediaType.APPLICATION_JSON));

		// Asserts
		result.andExpect(MockMvcResultMatchers.content().json("[]"));

	}

	@Test
	public void all_one_spider() throws Exception {

		// Arrange
		Mockito.when(spiderService.list()).thenReturn(Stream.of(new Spider().setName("generated")));

		// Actions
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/spiders").accept(MediaType.APPLICATION_JSON));

		// Asserts
		result.andExpect(MockMvcResultMatchers.content().json("[{\"name\":\"generated\"}]"));

	}

	@Test
	public void add_from_urls() throws Exception {

		// Arrange
		List<String> urls = Arrays.asList("http://toto");
		Spider spider = new Spider().setName("generated");
		Mockito.when(spiderService.add(urls)).thenReturn(spider);

		// Actions
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/spiders").param("urls", urls.toArray(new String[] {}))
				.accept(MediaType.APPLICATION_JSON));

		// Asserts
		Mockito.verify(spiderService).add(urls);
		Mockito.verifyNoMoreInteractions(spiderService);
		result.andExpect(MockMvcResultMatchers.content().json("{\"name\":\"generated\"}"));

	}

	@Test
	public void add_valid_spider() throws Exception {

		// Arrange
		Spider spider = new Spider().setName("test");
		Mockito.when(spiderService.add(spider)).thenReturn(spider);

		// Actions
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/spiders").content("{\"name\":\"test\"}").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));

		// Asserts
		Mockito.verify(spiderService).add(spider);
		Mockito.verifyNoMoreInteractions(spiderService);
		result.andExpect(MockMvcResultMatchers.content().json("{\"name\":\"test\"}"));

	}

	@Test
	public void update_spider() throws Exception {

		// Arrange
		Spider spider = new Spider().setName("test");
		Mockito.when(spiderService.update(spider)).thenReturn(spider);

		// Actions
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/spiders/0").content("{\"name\":\"test\"}").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));

		// Asserts
		Mockito.verify(spiderService).update(spider);
		Mockito.verifyNoMoreInteractions(spiderService);
		result.andExpect(MockMvcResultMatchers.content().json("{\"name\":\"test\"}"));

	}

	public void print(ResultActions result) throws IOException, UnsupportedEncodingException {
		System.err.println(IOUtils.toString(result.andReturn().getRequest().getInputStream()));
		CollectionUtils.toIterator(result.andReturn().getRequest().getHeaderNames()).forEachRemaining(
				h -> System.err.println(h + ":" + result.andReturn().getRequest().getHeader(h)));

		System.err.println(result.andReturn().getResponse().getContentAsString());
		result.andReturn().getResponse().getHeaderNames().forEach(h -> System.err.println(h + ":" + result.andReturn().getResponse().getHeader(h)));
	}
}
