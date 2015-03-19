package io.mandrel.common.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.mandrel.http.WebPage;
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
