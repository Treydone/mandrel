package io.mandrel.common.script;

import io.mandrel.common.WebPage;

import java.util.concurrent.Callable;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
@Slf4j
public class ScriptingService {

	private final Cache<Integer, CompiledScript> scripts;

	private final ScriptEngineManager sem;

	public ScriptingService() {
		ClassLoader classLoader = getClass().getClassLoader();

		sem = new ScriptEngineManager(classLoader);
		scripts = CacheBuilder.newBuilder().build();

		sem.getEngineFactories()
				.stream()
				.forEach(
						factory -> {
							log.debug("Engine : {}, version: {}, threading: {}", factory.getEngineName(), factory.getEngineVersion(),
									factory.getParameter("THREADING"));
						});
	}

	public Object execScript(String engineName, final String script, WebPage webPage, Object input) throws Exception {

		final ScriptEngine engine = getEngineByName(engineName);

		if (engine == null) {
			throw new UnknownScriptEngineException();
		}

		ScriptContext bindings = getBindings(webPage, input);

		return execScript(script, engine, bindings);
	}

	public Object execScript(final String script, final ScriptEngine engine, ScriptContext bindings) throws Exception {
		if (engine instanceof Compilable) {
			CompiledScript compiled = scripts.get(script.hashCode(), new Callable<CompiledScript>() {
				public CompiledScript call() throws Exception {
					return ((Compilable) engine).compile(script);
				}
			});
			return compiled.eval(bindings);
		} else {
			return engine.eval(script, bindings);
		}
	}

	public ScriptContext getBindings(WebPage webPage, Object input) {
		ScriptContext bindings = new SimpleScriptContext();
		bindings.setAttribute("input", input, ScriptContext.ENGINE_SCOPE);
		bindings.setAttribute("url", webPage.getUrl(), ScriptContext.ENGINE_SCOPE);
		return bindings;
	}

	public ScriptEngine getEngineByName(String engineName) {
		return sem.getEngineByName(engineName);
	}
}
