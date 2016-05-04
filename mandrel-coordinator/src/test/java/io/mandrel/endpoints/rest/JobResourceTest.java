///*
// * Licensed to Mandrel under one or more contributor
// * license agreements. See the NOTICE file distributed with
// * this work for additional information regarding copyright
// * ownership. Mandrel licenses this file to you under
// * the Apache License, Version 2.0 (the "License"); you may
// * not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package io.mandrel.endpoints.rest;
//
//import io.mandrel.common.data.Job;
//import io.mandrel.job.JobService;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//@RunWith(MockitoJUnitRunner.class)
//public class JobResourceTest {
//
//	@Mock
//	private JobService jobService;
//
//	private MockMvc mockMvc;
//
//	@Before
//	public void setUp() throws Exception {
//		mockMvc = MockMvcBuilders.standaloneSetup(new JobResource(null, jobService, null))
//				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).addFilter(new ApiOriginFilter(), "/*").build();
//	}
//
//	@Test
//	public void all_empty() throws Exception {
//
//		// Arrange
//		Pageable pageable = new PageRequest(0, 20);
//		Mockito.when(jobService.page(pageable)).thenReturn(new PageImpl<>(Collections.emptyList()));
//
//		// Actions
//		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(Apis.PREFIX + "/jobs").accept(MediaType.APPLICATION_JSON));
//
//		// Asserts
//		result.andDo(res -> {
//			System.err.println(res.getResponse().getContentAsString());
//		});
//		result.andExpect(MockMvcResultMatchers
//				.content()
//				.json("{\"content\":[],\"last\":true,\"totalPages\":1,\"totalElements\":0,\"sort\":null,\"first\":true,\"numberOfElements\":0,\"size\":0,\"number\":0}"));
//
//	}
//
//	@Test
//	public void all_one_job() throws Exception {
//
//		// Arrange
//		Pageable pageable = new PageRequest(0, 20);
//		Mockito.when(jobService.page(pageable)).thenReturn(new PageImpl<>(Collections.singletonList(new Job().setName("generated")), pageable, 1));
//
//		// Actions
//		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(Apis.PREFIX + "/jobs").accept(MediaType.APPLICATION_JSON));
//
//		// Asserts
//		result.andDo(res -> {
//			System.err.println(res.getResponse().getContentAsString());
//		});
//		result.andExpect(MockMvcResultMatchers
//				.content()
//				.json("{\"content\":[{\"name\":\"generated\"}],\"last\":true,\"totalPages\":1,\"totalElements\":1,\"sort\":null,\"first\":true,\"numberOfElements\":1,\"size\":20,\"number\":0}"));
//
//	}
//
//	@Test
//	public void add_from_urls() throws Exception {
//
//		// Arrange
//		List<String> urls = Arrays.asList("http://toto");
//		Job job = new Job().setName("generated");
//		Mockito.when(jobService.add(urls)).thenReturn(job);
//
//		// Actions
//		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(Apis.PREFIX + "/jobs").param("urls", urls.toArray(new String[] {}))
//				.accept(MediaType.APPLICATION_JSON));
//
//		// Asserts
//		Mockito.verify(jobService).add(urls);
//		Mockito.verifyNoMoreInteractions(jobService);
//		result.andExpect(MockMvcResultMatchers.content().json("{\"name\":\"generated\"}"));
//
//	}
//
//	@Test
//	public void add_valid_job() throws Exception {
//
//		// Arrange
//		Job job = new Job().setName("test");
//		Mockito.when(jobService.add(job)).thenReturn(job);
//
//		// Actions
//		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(Apis.PREFIX + "/jobs").content("{\"name\":\"test\"}")
//				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
//
//		// Asserts
//		Mockito.verify(jobService).add(job);
//		Mockito.verifyNoMoreInteractions(jobService);
//		result.andExpect(MockMvcResultMatchers.content().json("{\"name\":\"test\"}"));
//
//	}
//
//	@Test
//	public void update_job() throws Exception {
//
//		// Arrange
//		Job job = new Job().setName("test");
//		Mockito.when(jobService.update(job)).thenReturn(job);
//
//		// Actions
//		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(Apis.PREFIX + "/jobs/0").content("{\"name\":\"test\"}")
//				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
//
//		// Asserts
//		Mockito.verify(jobService).update(job);
//		Mockito.verifyNoMoreInteractions(jobService);
//		result.andExpect(MockMvcResultMatchers.content().json("{\"name\":\"test\"}"));
//
//	}
//
//}
