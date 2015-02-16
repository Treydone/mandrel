package io.mandrel.requester.ua;

import io.mandrel.common.data.Spider;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = FixedUserAgentProvisionner.class, name = "fixed") })
public interface UserAgentProvisionner extends Serializable {

	String get(String url, Spider spider);
}
