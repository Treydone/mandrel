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
package io.mandrel.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.mandrel.requests.WebPage;
import io.mandrel.script.ScriptingService;

import java.net.URL;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.junit.Test;

public class ScriptingTest {

	private ScriptingService scriptingService = new ScriptingService();

	@Test
	public void groovy() throws Exception {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok",
				null, null, null);

		ScriptEngine engine = scriptingService.getEngineByName("groovy");
		ScriptContext bindings = scriptingService.getBindings(webPage, null);

		// Actions
		Object result = scriptingService.execScript("println 'test'; 'echo'",
				engine, bindings);

		// Asserts
		assertNotNull(result);
		assertEquals("echo", result);
	}
}
