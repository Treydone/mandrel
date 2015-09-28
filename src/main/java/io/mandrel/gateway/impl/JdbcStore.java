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

import io.mandrel.requests.Bag;
import io.mandrel.requests.Metadata;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.core.IMap;

@Data
@EqualsAndHashCode(callSuper = false)
public class JdbcStore extends InternalStore {

	private static final long serialVersionUID = -4148862105449045170L;

	private Map<String, String> properties = new HashMap<>();

	@JsonProperty("table_name")
	private String tableName;

	@JsonProperty("create_query")
	private String createQuery = "create table if not exists {0} ( id varchar(255), content blob, primary key (id) )";

	@JsonProperty("insert_query")
	private String insertQuery = "insert into {0} (id, content) values ('?', '?')";

	@JsonProperty("select_key_query")
	private String selectKeyQuery = "select id from {0}";

	@JsonProperty("select_query")
	private String selectQuery = "select * from {0}";

	@JsonProperty("delete_query")
	private String deleteQuery = "delete from {0}";

	@JsonProperty("where_clause")
	private String whereClause = "where id in ";

	@JsonProperty("paging")
	private String paging = "from ?, ?";

	@JsonProperty("create")
	private boolean create = false;

	@JsonAnySetter
	public void add(String key, String value) {
		properties.put(key, value);
	}

	@Override
	public IMap<String, Bag<? extends Metadata>> getPageMap(long spiderId) {
		String mapKey = "pagestore-" + spiderId;
		prepare(mapKey);
		return hazelcastInstance.<String, Bag<? extends Metadata>> getMap(mapKey);
	}

	@Override
	public IMap<String, Metadata> getPageMetaMap(long spiderId) {
		String mapKey = "pagemetastore-" + spiderId;
		prepare(mapKey);
		return hazelcastInstance.<String, Metadata> getMap(mapKey);
	}

	public void prepare(String mapKey) {
		if (!hazelcastInstance.getConfig().getMapConfigs().containsKey(mapKey)) {
			MapConfig mapConfig = new MapConfig();
			MapStoreConfig mapStoreConfig = new MapStoreConfig();
			mapStoreConfig.setClassName(JdbcRawBackedMap.class.getName());
			mapStoreConfig.setFactoryClassName(JdbcRawBackMapFactory.class.getName());
			mapStoreConfig.setWriteBatchSize(1000);
			mapStoreConfig.setInitialLoadMode(InitialLoadMode.LAZY);
			mapStoreConfig.setWriteDelaySeconds(10);
			mapStoreConfig.setEnabled(true);

			this.properties.forEach((k, v) -> mapStoreConfig.setProperty(k, v));

			mapStoreConfig.setProperty("table_name", tableName);
			mapStoreConfig.setProperty("create_query", createQuery);
			mapStoreConfig.setProperty("create", Boolean.toString(create));
			mapStoreConfig.setProperty("insert_query", insertQuery);
			mapStoreConfig.setProperty("select_key_query", selectKeyQuery);
			mapStoreConfig.setProperty("select_query", selectQuery);
			mapStoreConfig.setProperty("delete_query", deleteQuery);
			mapStoreConfig.setProperty("where_clause", whereClause);
			mapStoreConfig.setProperty("paging", paging);

			mapConfig.setMapStoreConfig(mapStoreConfig);
			mapConfig.setName(mapKey);
			hazelcastInstance.getConfig().addMapConfig(mapConfig);
		}
	}

	@Override
	public String getType() {
		return "jdbc";
	}
}
