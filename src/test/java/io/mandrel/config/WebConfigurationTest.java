package io.mandrel.config;

import io.mandrel.endpoints.rest.ApiOriginFilter;
import io.mandrel.endpoints.rest.HomeResource;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = { WebConfiguration.class, BindConfiguration.class, HomeResource.class })
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class WebConfigurationTest {

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).addFilter(new ApiOriginFilter(), "/*").build();
	}

	@Test
	public void simple() throws Exception {

		// Arrange

		// Actions
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON));

		// Asserts
		result.andExpect(MockMvcResultMatchers.content().json("{\"ok\":\"isOk\"}"));

		Assertions.assertThat(result.andReturn().getResponse().getContentAsString()).contains("{\"ok\":\"isOk\"}");
		Assertions.assertThat(result.andReturn().getResponse().getContentAsString()).doesNotContain("{\n").doesNotContain("  \"ok\" : \"isOk\"")
				.doesNotContain("\n}");
	}

	@Test
	public void pretty() throws Exception {

		// Arrange

		// Actions
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/").param("pretty", "").accept(MediaType.APPLICATION_JSON));

		// Asserts
		result.andExpect(MockMvcResultMatchers.content().json("{\"ok\":\"isOk\"}"));

		Assertions.assertThat(result.andReturn().getResponse().getContentAsString()).doesNotContain("{\"ok\":\"isOk\"}");
		Assertions.assertThat(result.andReturn().getResponse().getContentAsString()).contains("{\n").contains("  \"ok\" : \"isOk\"").contains("\n}");
	}

}
