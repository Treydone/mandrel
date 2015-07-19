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
package io.mandrel.monitor;

import io.mandrel.monitor.SigarService;
import io.mandrel.monitor.SigarServiceTest.LocalConfiguration;

import javax.inject.Inject;

import org.hyperic.sigar.SigarException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = LocalConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SigarServiceTest {

	@Configuration
	public static class LocalConfiguration {

		@Bean
		public SigarService sigarService(ResourceLoader loader) {
			return new SigarService(loader);
		}
	}

	@Inject
	private SigarService sigarService;

	@Test
	public void test() throws SigarException {

		System.err.println(sigarService.infos());

	}
}
