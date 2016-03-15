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

import io.mandrel.common.data.Client;
import io.mandrel.common.data.Extractors;
import io.mandrel.common.data.Spider;
import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.common.loader.NamedProviders;
import io.mandrel.data.filters.link.LinkFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

public class SchemaTest {

	@Test
	public void test() throws JsonProcessingException {

		ObjectMapper m = new ObjectMapper();
		SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
		m.acceptJsonFormatVisitor(m.constructType(Spider.class), visitor);
		JsonSchema jsonSchema = visitor.finalSchema();

		System.err.println(m.writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema));
	}

	@Test
	public void test1() {

		int level = 0;

		inspect(level, Extractors.class, "root");

	}

	public void inspect(int level, Type clazz, String name) {

		if (level > 6)
			return;
		if (clazz instanceof Class && clazz.equals(LinkFilter.class))
			return;

		if (clazz instanceof Class && !((Class<?>) clazz).isEnum() && ((Class<?>) clazz).getPackage() != null
				&& ((Class<?>) clazz).getPackage().getName().startsWith("io.mandrel")) {
			int newLevel = level + 1;

			List<Field> fields = new ArrayList<Field>();
			Class<?> i = ((Class<?>) clazz);
			while (i != null && i != Object.class) {
				fields.addAll(Arrays.asList(i.getDeclaredFields()));
				i = i.getSuperclass();
			}

			for (Field field : fields) {
				Class<?> fieldType = field.getType();
				String text;
				if (!field.isAnnotationPresent(JsonIgnore.class) && !Modifier.isStatic(field.getModifiers())) {
					if (List.class.equals(fieldType) || Map.class.equals(fieldType)) {
						Type type = field.getGenericType();
						if (type instanceof ParameterizedType) {
							ParameterizedType pType = (ParameterizedType) type;
							for (Type paramType : pType.getActualTypeArguments()) {
								if (paramType instanceof Class && NamedDefinition.class.isAssignableFrom((Class) paramType)) {
									text = field.getName() + "(container of " + paramType + " oneOf)";
									System.err.println(StringUtils.leftPad(text, text.length() + newLevel * 5, "\t-   "));
									Class<? extends NamedDefinition> nd = (Class<? extends NamedDefinition>) field.getType();
									Map<String, ? extends NamedDefinition> map = NamedProviders.get(nd);
									map.forEach((k, v) -> {
										String text2 = k;
										System.err.println(StringUtils.leftPad(text2, text2.length() + newLevel * 5, "\t-   "));
										inspect(newLevel, v.getClass(), field.getName());
									});
								} else if (paramType instanceof ParameterizedType
										&& NamedDefinition.class.isAssignableFrom((Class) ((ParameterizedType) paramType).getRawType())) {
									text = field.getName() + "(container of " + paramType + " oneOf2)";
									System.err.println(StringUtils.leftPad(text, text.length() + newLevel * 5, "\t-   "));
									Class<? extends NamedDefinition> nd = (Class<? extends NamedDefinition>) ((ParameterizedType) paramType).getRawType();
									Map<String, ? extends NamedDefinition> map = NamedProviders.get(nd);
									map.forEach((k, v) -> {
										String text2 = k;
										System.err.println(StringUtils.leftPad(text2, text2.length() + newLevel * 5, "\t-   "));
										inspect(newLevel, v.getClass(), field.getName());
									});
								} else if (paramType instanceof WildcardType) {
									for (Type wildType : ((WildcardType) paramType).getUpperBounds()) {
										if (wildType instanceof Class && NamedDefinition.class.isAssignableFrom((Class) wildType)) {
											text = field.getName() + "(container of " + wildType + " oneOf)";
											System.err.println(StringUtils.leftPad(text, text.length() + newLevel * 5, "\t-   "));
											Class<? extends NamedDefinition> nd = (Class<? extends NamedDefinition>) field.getType();
											Map<String, ? extends NamedDefinition> map = NamedProviders.get(nd);
											map.forEach((k, v) -> {
												String text2 = k;
												System.err.println(StringUtils.leftPad(text2, text2.length() + newLevel * 5, "\t-   "));
												inspect(newLevel, v.getClass(), field.getName());
											});
										} else if (wildType instanceof ParameterizedType
												&& NamedDefinition.class.isAssignableFrom((Class) ((ParameterizedType) wildType).getRawType())) {
											text = field.getName() + "(container of " + wildType + " oneOf2)";
											System.err.println(StringUtils.leftPad(text, text.length() + newLevel * 5, "\t-   "));
											Class<? extends NamedDefinition> nd = (Class<? extends NamedDefinition>) ((ParameterizedType) wildType)
													.getRawType();
											Map<String, ? extends NamedDefinition> map = NamedProviders.get(nd);
											map.forEach((k, v) -> {
												String text2 = k;
												System.err.println(StringUtils.leftPad(text2, text2.length() + newLevel * 5, "\t-   "));
												inspect(newLevel, v.getClass(), field.getName());
											});
										}
									}
								} else {
									text = field.getName() + "(container of " + paramType + ")";
									System.err.println(StringUtils.leftPad(text, text.length() + newLevel * 5, "\t-   "));
									inspect(newLevel, paramType, "");
								}
							}
						}
					} else {
						if (NamedDefinition.class.isAssignableFrom(field.getType())) {
							text = field.getName() + " oneOf";
							System.err.println(StringUtils.leftPad(text, text.length() + newLevel * 5, "\t-   "));
							Class<? extends NamedDefinition> nd = (Class<? extends NamedDefinition>) field.getType();
							Map<String, ? extends NamedDefinition> map = NamedProviders.get(nd);
							map.forEach((k, v) -> {
								String text2 = k;
								System.err.println(StringUtils.leftPad(text2, text2.length() + newLevel * 5, "\t-   "));
								inspect(newLevel, v.getClass(), field.getName());
							});
						} else {
							text = field.getName()
									+ (field.getType().isPrimitive() || field.getType().equals(String.class) || field.getType().equals(LocalDateTime.class) ? " ("
											+ field.getType().getName() + ")"
											: "");
							System.err.println(StringUtils.leftPad(text, text.length() + newLevel * 5, "\t-   "));
							inspect(newLevel, fieldType, field.getName());
						}
					}

					// JsonSubTypes subtype =
					// fieldType.getAnnotation(JsonSubTypes.class);
					// if (subtype != null) {
					// int subLevel = level + 2;
					// text = "subtypes:";
					// System.err.println(StringUtils.leftPad(text,
					// text.length() + subLevel * 5, "\t-   "));
					// for (JsonSubTypes.Type type : subtype.value()) {
					// text = "subtype:" + type.name();
					// System.err.println(StringUtils.leftPad(text,
					// text.length() + (subLevel + 1) * 5, "\t-   "));
					// inspect((subLevel + 1), type.value(), type.name());
					// }
					// }
				}
			}

			JsonSubTypes subtype = ((Class<?>) clazz).getAnnotation(JsonSubTypes.class);
			if (subtype != null) {
				int subLevel = level + 1;
				String text = "subtypes:";
				System.err.println(StringUtils.leftPad(text, text.length() + subLevel * 5, "\t-   "));
				for (JsonSubTypes.Type type : subtype.value()) {
					text = "subtype:" + type.name();
					System.err.println(StringUtils.leftPad(text, text.length() + (subLevel + 1) * 5, "\t-   "));
					inspect((subLevel + 1), type.value(), type.name());
				}
			}
		}
	}
}
