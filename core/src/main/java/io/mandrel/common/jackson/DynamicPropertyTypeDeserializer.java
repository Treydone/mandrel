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
package io.mandrel.common.jackson;

import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.common.loader.NamedProviders;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.type.SimpleType;

public class DynamicPropertyTypeDeserializer extends AsPropertyTypeDeserializer {

	private static final long serialVersionUID = -756408332548345513L;

	public DynamicPropertyTypeDeserializer(final JavaType bt, final TypeIdResolver idRes, final String typePropertyName, final boolean typeIdVisible,
			final Class<?> defaultImpl) {
		super(bt, idRes, typePropertyName, typeIdVisible, defaultImpl);
	}

	public DynamicPropertyTypeDeserializer(final AsPropertyTypeDeserializer src, final BeanProperty property) {
		super(src, property);
	}

	@Override
	public TypeDeserializer forProperty(final BeanProperty prop) {
		return (prop == _property) ? this : new DynamicPropertyTypeDeserializer(this, prop);
	}

	@Override
	public Object deserializeTypedFromObject(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
		JsonNode node = jp.readValueAsTree();

		NamedDefinition res = NamedProviders.get((Class<NamedDefinition>) _baseType.getRawClass(), node.get("type").asText());
		JavaType type = SimpleType.construct(res.getClass());

		JsonParser jsonParser = new TreeTraversingParser(node, jp.getCodec());
		if (jsonParser.getCurrentToken() == null) {
			jsonParser.nextToken();
		}
		JsonDeserializer<Object> deser = ctxt.findContextualValueDeserializer(type, _property);
		return deser.deserialize(jsonParser, ctxt);
	}
}