package io.mandrel.common.jackson;

import io.mandrel.common.loader.NamedComponent;

import java.util.Collection;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.TypeNameIdResolver;

public class DynamicPropertyTypeResolver extends StdTypeResolverBuilder {
	@Override
	public TypeDeserializer buildTypeDeserializer(final DeserializationConfig config, final JavaType baseType, final Collection<NamedType> subtypes) {
		return new DynamicPropertyTypeDeserializer(baseType, null, _typeProperty, _typeIdVisible, _defaultImpl);
	}

	@Override
	public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
		TypeIdResolver idRes = TypeNameIdResolver.construct(config, baseType, subtypes, true, false);
		return new AsPropertyTypeSerializer(idRes, null, "type") {

			protected String idFromValue(Object value) {
				return ((NamedComponent) value).name();
			}
		};
	}
}
