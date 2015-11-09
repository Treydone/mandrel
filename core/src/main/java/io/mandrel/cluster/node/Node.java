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
package io.mandrel.cluster.node;

import io.mandrel.monitor.Infos;

import java.io.Serializable;
import java.net.URI;
import java.util.Base64;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;

@Data
@Accessors(chain = true)
public class Node implements Serializable {
	private static final long serialVersionUID = 9044434196832084086L;

	private URI uri;
	private Infos infos;
	private String type;

	@JsonProperty("_id")
	public String getId() {
		return idOf(uri);
	}

	public static String idOf(URI uri) {
		return uri != null ? Base64.getUrlEncoder().encodeToString(uri.toString().getBytes(Charsets.UTF_8)) : null;
	}
}
