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

import io.mandrel.metadata.FetchMetadata;

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

	public Object execScript(String engineName, final String script, FetchMetadata data, Object input) throws Exception {

		final ScriptEngine engine = getEngineByName(engineName);

		if (engine == null) {
			throw new UnknownScriptEngineException();
		}

		ScriptContext bindings = getBindings(data, input);

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

	public ScriptContext getBindings(FetchMetadata data, Object input) {
		ScriptContext bindings = new SimpleScriptContext();
		bindings.setAttribute("input", input, ScriptContext.ENGINE_SCOPE);
		bindings.setAttribute("uri", data.getUri(), ScriptContext.ENGINE_SCOPE);
		return bindings;
	}

	public ScriptEngine getEngineByName(String engineName) {
		return sem.getEngineByName(engineName);
	}
}
