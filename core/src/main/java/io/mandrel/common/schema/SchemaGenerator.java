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
package io.mandrel.common.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SchemaGenerator {

	private final ObjectMapper objectMapper;

	public ObjectNode getSchema(Class<?> clazz) throws JsonMappingException {

		ObjectNode root = objectMapper.createObjectNode();

		ObjectNode schema = objectMapper.createObjectNode();
		ArrayNode form = objectMapper.createArrayNode();

		getSchema(clazz, schema, form);

		form.add("*");
		form.addPOJO(objectMapper.createObjectNode().put("type", "submit").put("value", "Submit"));
		root.set("schema", schema);
		root.set("form", form);
		return root;
	}

	protected void getSchema(Class<?> clazz, ObjectNode schema, ArrayNode form) throws JsonMappingException {

		SchemaFactoryWrapper factoryWrapper = new SchemaFactoryWrapper();

		JsonSubTypes subtype = clazz.getAnnotation(JsonSubTypes.class);
		if (subtype != null) {
			doForSubtype(factoryWrapper, schema, form, subtype);
		} else {
			objectMapper.acceptJsonFormatVisitor(objectMapper.constructType(clazz), factoryWrapper);
			JsonSchema jsonSchema = factoryWrapper.finalSchema();
			jsonSchema.setId(null);
			String title = clazz.getSimpleName().replaceAll("\\.", "_");
			jsonSchema.asObjectSchema().setTitle(title);

			iterateOnProperties(jsonSchema.asObjectSchema().getProperties());
			schema.putPOJO(title, jsonSchema);
		}
	}

	public void doForSubtype(SchemaFactoryWrapper factoryWrapper, ObjectNode schema, ArrayNode form, JsonSubTypes subtype) throws JsonMappingException {
		List<String> types = new ArrayList<>();
		for (JsonSubTypes.Type type : subtype.value()) {
			objectMapper.acceptJsonFormatVisitor(objectMapper.constructType(type.value()), factoryWrapper);
			JsonSchema jsonSchema = factoryWrapper.finalSchema();
			jsonSchema.setId(null);
			String title = type.name().replaceAll("\\.", "_");
			jsonSchema.asObjectSchema().setTitle(title);

			iterateOnProperties(jsonSchema.asObjectSchema().getProperties());
			schema.putPOJO(title, jsonSchema);
			types.add(title);
		}

		ObjectNode choice = objectMapper.createObjectNode();
		choice.put("type", "string");
		choice.putPOJO("enum", types);
		schema.set("choice", choice);

		form.addPOJO(objectMapper
				.createObjectNode()
				.put("type", "fieldset")
				.put("title", "Sources")
				.set("items",
						objectMapper.createArrayNode().add(
								objectMapper.createObjectNode().put("type", "selectfieldset").put("key", "choice").put("title", "Choose a source")
										.putPOJO("items", types))));
	}

	public void iterateOnProperties(Map<String, JsonSchema> properties) {
		properties.remove("type");
		for (Entry<String, JsonSchema> entry : properties.entrySet()) {
			entry.getValue().setId(null);
			if (entry.getValue().isSimpleTypeSchema()) {
				entry.getValue().asSimpleTypeSchema().setTitle(entry.getKey());
				if (entry.getValue().isObjectSchema()) {
					iterateOnProperties(entry.getValue().asObjectSchema().getProperties());
				}
				if (entry.getValue().isArraySchema()) {
					JsonSchema arraySchema = entry.getValue().asArraySchema().getItems().asSingleItems().getSchema();
					arraySchema.setId(null);
					if (arraySchema.isSimpleTypeSchema()) {
						arraySchema.asSimpleTypeSchema().setTitle(entry.getKey().substring(0, entry.getKey().length() - 1));
						if (arraySchema.isObjectSchema()) {
							iterateOnProperties(arraySchema.asObjectSchema().getProperties());
						}
					}
				}
			}
		}
	}
}
