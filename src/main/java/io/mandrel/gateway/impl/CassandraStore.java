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
package io.mandrel.gateway.impl;

import io.mandrel.common.data.Politeness;
import io.mandrel.data.spider.Link;
import io.mandrel.gateway.PageMetadataStore;
import io.mandrel.gateway.WebPageStore;
import io.mandrel.http.Metadata;
import io.mandrel.http.WebPage;

import java.util.Map;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;

@Data
public class CassandraStore implements WebPageStore, PageMetadataStore {

	private static final long serialVersionUID = 6800608875261746768L;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private transient HazelcastInstance hazelcastInstance;

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void init(Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	public void addPage(long spiderId, String url, WebPage webPage) {

	}

	@Override
	public void addMetadata(long spiderId, String url, Metadata metadata) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> filter(long spiderId, Set<Link> outlinks, Politeness politeness) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAllFor(long spiderId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void byPages(long spiderId, int pageSize, Callback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public Metadata getMetadata(long spiderId, String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebPage getPage(long spiderId, String url) {
		// TODO Auto-generated method stub
		return null;
	}
}
