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

import io.mandrel.common.bson.JsonBsonCodec;
import io.mandrel.common.data.HttpStrategy.HttpStrategyDefinition;
import io.mandrel.common.data.Spider;
import io.mandrel.common.schema.SchemaGenerator;
import io.mandrel.config.BindConfiguration;
import io.mandrel.data.filters.link.LinkFilter;
import io.mandrel.requests.ftp.FtpRequester.FtpRequesterDefinition;
import io.mandrel.requests.http.ApacheHttpRequester.ApacheHttpRequesterDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

/**
 *
 */
public class OtherTest {

	@Test
	@SneakyThrows
	public void whut4() {
		String add = "208.67.220.220:53";
		Splitter splitter = Splitter.on(":");
		List<String> split = Lists.newArrayList(splitter.split(add));
		InetAddresses.forString(split.get(0));
		new InetSocketAddress(InetAddress.getByAddress(split.get(0).getBytes(Charsets.UTF_8)), split.size() == 2 ? Integer.valueOf(split.get(1)) : 53);

	}

	@Test
	@SneakyThrows
	public void whut2() throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		BindConfiguration.configure(mapper);

		Spider value = new Spider();
		value.setId(12);
		String res = mapper.writeValueAsString(value);
		System.err.println(res);

		Document doc = JsonBsonCodec.toBson(mapper, value);

		// System.err.println(System.currentTimeMillis());
		MongoClient mongo = new MongoClient();
		MongoCollection<Document> collection = mongo.getDatabase("mandrel").getCollection("test");
//		collection.insertOne(doc);

		Spider result = collection.find().map(el -> {
			try {
				// System.err.println(System.currentTimeMillis());
				String json = el.toJson();
				System.err.println(json);
				// System.err.println(System.currentTimeMillis());
				Spider readValue = JsonBsonCodec.fromBson(mapper, doc, Spider.class);
				// System.err.println(System.currentTimeMillis());
				return readValue;
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}).first();
		// System.err.println(System.currentTimeMillis());
		System.err.println(((ApacheHttpRequesterDefinition) result.getClient().getRequesters().get(0)));
		System.err.println(((HttpStrategyDefinition) ((ApacheHttpRequesterDefinition) result.getClient().getRequesters().get(0)).getStrategy())
				.getMaxRedirects());
		System.err.println(((FtpRequesterDefinition) result.getClient().getRequesters().get(1)));
		System.err.println(((FtpRequesterDefinition) result.getClient().getRequesters().get(1)).getStrategy().getNameResolver());
	}

	@Test
	public void test() throws JsonProcessingException {

		ObjectMapper objectMapper = new ObjectMapper();

		Class<?> clazz = LinkFilter.class;
		for (JsonSubTypes.Type type : clazz.getAnnotation(JsonSubTypes.class).value()) {
			System.err.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new SchemaGenerator(objectMapper).getSchema(type.value())));
		}

	}

	@Test
	public void test1() {

		int level = 0;
		Class<Spider> clazz = Spider.class;

		inspect(level, clazz, "root");

	}

	public void inspect(int level, Type clazz, String name) {

		if (level > 6)
			return;

		if (clazz instanceof Class && !((Class<?>) clazz).isEnum() && ((Class<?>) clazz).getPackage() != null
				&& ((Class<?>) clazz).getPackage().getName().startsWith("io.mandrel")) {
			int newLevel = level + 1;
			Field[] fields = ((Class<?>) clazz).getDeclaredFields();

			for (Field field : fields) {
				Class<?> fieldType = field.getType();
				String text;
				if (!field.isAnnotationPresent(JsonIgnore.class) && !Modifier.isStatic(field.getModifiers())) {
					if (List.class.equals(fieldType) || Map.class.equals(fieldType)) {
						Type type = field.getGenericType();
						if (type instanceof ParameterizedType) {
							ParameterizedType pType = (ParameterizedType) type;
							Type paramType = pType.getActualTypeArguments()[pType.getActualTypeArguments().length - 1];

							text = field.getName() + "(container of " + paramType + ")";
							System.err.println(StringUtils.leftPad(text, text.length() + newLevel * 5, "\t-   "));
							inspect(newLevel, paramType, "");
						}
					} else {
						text = field.getName()
								+ (field.getType().isPrimitive() || field.getType().equals(String.class) || field.getType().equals(LocalDateTime.class) ? " ("
										+ field.getType().getName() + ")" : "");
						System.err.println(StringUtils.leftPad(text, text.length() + newLevel * 5, "\t-   "));
						inspect(newLevel, fieldType, field.getName());
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
