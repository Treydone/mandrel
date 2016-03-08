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
package io.mandrel.common.settings;

import static org.junit.Assert.*;
import io.mandrel.common.settings.SettingsTest.SettingsConfiguration;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = SettingsConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SettingsTest {

	@Configuration
	@EnableConfigurationProperties(InfoSettings.class)
	public static class SettingsConfiguration {

	}

	@Inject
	private InfoSettings settings;

	@Test
	public void test() {

		assertNotNull(settings.getVersion());

	}
}
