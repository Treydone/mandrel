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

import io.mandrel.http.WebPage;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Throwables;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStoreFactory;

@Slf4j
public class JdbcRawBackMapFactory implements MapStoreFactory<String, WebPage> {

	@Override
	public MapLoader<String, WebPage> newMapStore(String mapName, Properties properties) {
		Map<String, String> configuration = properties.entrySet().stream().collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue().toString()));

		PoolConfiguration pool = new PoolProperties();
		try {
			BeanUtils.populate(pool, configuration);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw Throwables.propagate(e);
		}

		org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource(pool);
		try {
			BeanUtils.populate(dataSource, configuration);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw Throwables.propagate(e);
		}

		log.debug("Connecting datasource to: " + dataSource.getUrl() + " using " + dataSource.getDriverClassName());

		String tableName = properties.getProperty("table_name");

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		if (Boolean.valueOf(properties.getProperty("create", "false"))) {
			jdbcTemplate.update(MessageFormat.format(properties.getProperty("create_query"), tableName));
		}

		JdbcRawBackedMap backedMap = new JdbcRawBackedMap(jdbcTemplate, tableName, MessageFormat.format(properties.getProperty("insert_query"), tableName),
				MessageFormat.format(properties.getProperty("select_key_query"), tableName), MessageFormat.format(properties.getProperty("select_query"),
						tableName), MessageFormat.format(properties.getProperty("delete_query"), tableName), properties.getProperty("where_clause"),
				properties.getProperty("paging"));
		return backedMap;
	}
}