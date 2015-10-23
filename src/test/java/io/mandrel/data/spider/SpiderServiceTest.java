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
package io.mandrel.data.spider;

import static org.junit.Assert.assertEquals;
import io.mandrel.common.data.Client;
import io.mandrel.common.data.Filters;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.StoresDefinition;
import io.mandrel.config.BindConfiguration;
import io.mandrel.data.filters.link.AllowedForDomainsFilter;
import io.mandrel.data.filters.link.LinkFilter;
import io.mandrel.data.filters.link.UrlPatternFilter;
import io.mandrel.data.filters.page.DataObjectFilter;
import io.mandrel.data.filters.page.LargeFilter;
import io.mandrel.data.spider.SpiderServiceTest.LocalConfiguration;
import io.mandrel.data.validation.Validators;

import java.io.IOException;
import java.util.Arrays;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.Errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

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

		StoresDefinition stores = new StoresDefinition();

		String json = objectMapper.writeValueAsString(stores);
		System.err.println(json);
		StoresDefinition read = objectMapper.readValue(json, StoresDefinition.class);
		assertEquals(stores, read);
	}

	@Test
	public void filters() throws IOException {

		Filters filters = new Filters();

		String json = objectMapper.writeValueAsString(filters);
		System.err.println(json);
		Filters read = objectMapper.readValue(json, Filters.class);
		assertEquals(filters, read);
	}

	@Test
	public void linkFilters_domain() throws IOException {

		// URL
		AllowedForDomainsFilter filter = new AllowedForDomainsFilter();
		filter.domains(Arrays.asList("wiki.org"));

		String json = objectMapper.writeValueAsString(filter);
		System.err.println(json);
		LinkFilter read = objectMapper.readValue(json, LinkFilter.class);
		assertEquals(filter, read);
	}

	@Test
	public void linkFilters_pattern() throws IOException {

		// URL
		UrlPatternFilter filter = new UrlPatternFilter();
		filter.setValue(".*");

		String json = objectMapper.writeValueAsString(filter);
		System.err.println(json);
		LinkFilter read = objectMapper.readValue(json, LinkFilter.class);
		assertEquals(filter, read);
	}

	@Test
	public void pageFilters_large() throws IOException {

		// URL
		LargeFilter filter = new LargeFilter();

		String json = objectMapper.writeValueAsString(filter);
		System.err.println(json);
		DataObjectFilter read = objectMapper.readValue(json, DataObjectFilter.class);
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

		Errors errors = Validators.validate(spider);

		System.err.println(errors);

	}

	@Test
	public void schema() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
		objectMapper.acceptJsonFormatVisitor(objectMapper.constructType(Spider.class), visitor);
		JsonSchema jsonSchema = visitor.finalSchema();

		System.err.println(objectMapper.writeValueAsString(jsonSchema));
	}
}