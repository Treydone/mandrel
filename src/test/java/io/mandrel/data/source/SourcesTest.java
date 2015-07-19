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
package io.mandrel.data.source;

import static org.junit.Assert.assertEquals;
import io.mandrel.config.BindConfiguration;
import io.mandrel.data.source.SourcesTest.LocalConfiguration;

import java.io.IOException;
import java.util.Arrays;

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
public class SourcesTest {

	@Configuration
	@Import(BindConfiguration.class)
	public static class LocalConfiguration {

	}

	@Inject
	private ObjectMapper objectMapper;

	@Test
	public void seed() throws IOException {

		FixedSource source = new FixedSource();
		source.setUrls(Arrays.asList("test"));

		String json = objectMapper.writeValueAsString(source);
		System.err.println(json);
		Source read = objectMapper.readValue(json, Source.class);
		assertEquals(source, read);
	}

	@Test
	public void jdbc() throws IOException {

		JdbcSource source = new JdbcSource();
		source.setQuery("select * from test");
		source.setUrl("url");

		String json = objectMapper.writeValueAsString(source);
		System.err.println(json);
		Source read = objectMapper.readValue(json, Source.class);
		assertEquals(source, read);
	}

	@Test
	public void jms() throws IOException {

		JmsSource source = new JmsSource();
		source.setUrl("url");

		String json = objectMapper.writeValueAsString(source);
		System.err.println(json);
		Source read = objectMapper.readValue(json, Source.class);
		assertEquals(source, read);
	}
}
