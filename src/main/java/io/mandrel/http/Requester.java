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
package io.mandrel.http;

import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Strategy;
import io.mandrel.common.lifecycle.Initializable;

import java.io.Closeable;

import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = HCRequester.class, name = "hc") })
public abstract class Requester implements Closeable, Initializable {

	@JsonIgnore
	@Setter
	protected Strategy strategy;

	public abstract void get(String url, Spider spider, SuccessCallback successCallback, FailureCallback failureCallback);

	@Deprecated
	public abstract WebPage getBlocking(String url, Spider spider) throws Exception;

	@Deprecated
	public abstract WebPage getBlocking(String url) throws Exception;

	@FunctionalInterface
	public static interface SuccessCallback {
		void on(WebPage webapge);
	}

	@FunctionalInterface
	public static interface FailureCallback {
		void on(Throwable t);
	}

	public abstract String getType();
}
