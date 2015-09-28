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
package io.mandrel.data.filters;

import static org.junit.Assert.assertEquals;
import io.mandrel.config.BindConfiguration;
import io.mandrel.data.filters.WebPageFiltersTest.LocalConfiguration;
import io.mandrel.data.filters.page.LargeFilter;
import io.mandrel.data.filters.page.DataObjectFilter;
import io.mandrel.data.filters.page.BooleanDataObjectFilters.NotFilter;

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
	public void large() throws IOException {

		LargeFilter filter = new LargeFilter();

		String json = objectMapper.writeValueAsString(filter);
		System.err.println(json);
		DataObjectFilter read = objectMapper.readValue(json, DataObjectFilter.class);
		assertEquals(filter, read);
	}

	@Test
	public void not() throws IOException {

		NotFilter filter = new NotFilter();
		filter.setFilter(new LargeFilter());

		String json = objectMapper.writeValueAsString(filter);
		System.err.println(json);
		DataObjectFilter read = objectMapper.readValue(json, DataObjectFilter.class);
		assertEquals(filter, read);
	}
}
