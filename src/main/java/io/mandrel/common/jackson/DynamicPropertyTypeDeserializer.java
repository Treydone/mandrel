package io.mandrel.common.jackson;

import io.mandrel.common.loader.NamedComponent;
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

		NamedComponent res = NamedProviders.get((Class<NamedComponent>) _baseType.getRawClass(), node.get("type").asText());
		JavaType type = SimpleType.construct(res.getClass());

		JsonParser jsonParser = new TreeTraversingParser(node, jp.getCodec());
		if (jsonParser.getCurrentToken() == null) {
			jsonParser.nextToken();
		}
		JsonDeserializer<Object> deser = ctxt.findContextualValueDeserializer(type, _property);
		return deser.deserialize(jsonParser, ctxt);
	}
}