package io.mandrel.common.loader;

import io.mandrel.common.jackson.DynamicPropertyTypeResolver;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonTypeResolver(DynamicPropertyTypeResolver.class)
public interface NamedComponent {

	String name();
}
