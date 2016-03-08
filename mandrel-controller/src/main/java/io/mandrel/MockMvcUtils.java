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
package io.mandrel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.CollectionUtils;

public abstract class MockMvcUtils {

	public static void print(ResultActions result) throws IOException, UnsupportedEncodingException {
		System.err.println(IOUtils.toString(result.andReturn().getRequest().getInputStream()));
		CollectionUtils.toIterator(result.andReturn().getRequest().getHeaderNames()).forEachRemaining(
				h -> System.err.println(h + ":" + result.andReturn().getRequest().getHeader(h)));

		System.err.println(result.andReturn().getResponse().getContentAsString());
		result.andReturn().getResponse().getHeaderNames().forEach(h -> System.err.println(h + ":" + result.andReturn().getResponse().getHeader(h)));
	}
}
