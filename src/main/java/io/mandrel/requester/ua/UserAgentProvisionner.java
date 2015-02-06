package io.mandrel.requester.ua;

import io.mandrel.service.spider.Spider;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = FixedUserAgentProvisionner.class, name = "fixed") })
public interface UserAgentProvisionner {

	String get(String url, Spider spider);
}
