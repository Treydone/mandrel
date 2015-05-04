package io.mandrel.http.proxy;

import io.mandrel.common.data.Spider;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ning.http.client.ProxyServer;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = NoProxyProxyServersSource.class, name = "no") })
public interface ProxyServersSource extends Serializable {

	// TODO Use an internal proxy class
	ProxyServer findProxy(Spider spider);
}
