package io.mandrel.requester.proxy;

import io.mandrel.service.spider.Spider;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ning.http.client.ProxyServer;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = NoProxyProxyServersSource.class, name = "no") })
public interface ProxyServersSource {

	ProxyServer findProxy(Spider spider);
}
