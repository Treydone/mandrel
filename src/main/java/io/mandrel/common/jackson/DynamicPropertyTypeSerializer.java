package io.mandrel.common.jackson;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsExistingPropertyTypeSerializer;

public class DynamicPropertyTypeSerializer extends AsExistingPropertyTypeSerializer {

	public DynamicPropertyTypeSerializer(TypeIdResolver idRes, BeanProperty property, String propName) {
		super(idRes, property, propName);
	}

	
}
