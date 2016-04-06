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

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RestConfiguration extends WebMvcConfigurerAdapter {

	@Autowired
	private ObjectMapper mapper;

	@Bean
	@ConditionalOnMissingBean
	public DefaultErrorAttributes errorAttributes() {
		return new DefaultErrorAttributes();
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.replaceAll(c -> {
			if (c instanceof MappingJackson2HttpMessageConverter) {
				MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper) {
					protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
						RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
						if (attributes != null && attributes instanceof ServletRequestAttributes) {
							String attribute = ((ServletRequestAttributes) attributes).getRequest().getParameter("pretty");
							if (attribute != null) {
								generator.setPrettyPrinter(new DefaultPrettyPrinter());
							}
						}
						super.writePrefix(generator, object);
					}
				};
				return converter;
			} else {
				return c;
			}
		});
	}
}
