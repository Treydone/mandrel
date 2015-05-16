package io.mandrel.http.dns;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = InternalNameResolver.class, name = "simple"), @Type(value = CachedNameResolver.class, name = "cached") })
public interface NameResolver extends Serializable {

	InetAddress resolve(String host) throws UnknownHostException;
}
