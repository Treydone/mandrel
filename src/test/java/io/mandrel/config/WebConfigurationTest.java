/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
