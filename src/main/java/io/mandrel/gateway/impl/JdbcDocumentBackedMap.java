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

import io.mandrel.data.content.FieldExtractor;
import io.mandrel.data.content.MetadataExtractor;
import io.mandrel.gateway.Document;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;

@Data
public class JdbcDocumentBackedMap implements MapStore<String, Document>, MapLoader<String, Document> {

	private final JdbcTemplate jdbcTemplate;

	private final String tableName;

	private final String selectKeyQuery;

	private final String selectQuery;

	private final String deleteQuery;

	private final String whereClause;

	private final String paging;

	private final boolean onlyFirstElement;

	@JsonIgnore
	private MetadataExtractor webPageExtractor;

	private RowMapper<Document> getRowMapper() {
		return (row, nb) -> {
			Document doc = new Document();

			for (int i = 0; i < row.getMetaData().getColumnCount(); i++) {
				String column = row.getMetaData().getColumnName(i);
				List<Object> results = new ArrayList<>();

				if (onlyFirstElement) {
					results.add(row.getString(i));
				} else {
					Object[] array = (Object[]) row.getArray(i).getArray();
					results.add(Lists.newArrayList(array));
				}
				doc.put(column, results);
			}
			return doc;
		};
	}

	@Override
	public Document load(String key) {
		try {
			return jdbcTemplate.queryForObject(selectKeyQuery + " " + whereClause + " ('" + key + "')", getRowMapper());
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	@Override
	public Map<String, Document> loadAll(Collection<String> keys) {
		List<Document> results = jdbcTemplate.query(selectQuery + " " + whereClause + " ('" + StringUtils.join(keys, "','") + "')", getRowMapper());
		if (results != null) {
			return results.stream().collect(Collectors.toMap(w -> w.getId(), w -> w));
		}
		return null;

	}

	@Override
	public Set<String> loadAllKeys() {
		List<Document> results = jdbcTemplate.query(selectQuery, getRowMapper());
		if (results != null) {
			return results.stream().map(w -> w.getId()).collect(Collectors.toSet());
		}
		return null;
	}

	@Override
	public void store(String key, Document value) {
		StringBuilder builder = new StringBuilder();
		List<Object> values = new ArrayList<>(webPageExtractor.getFields().size());
		int[] types = new int[webPageExtractor.getFields().size()];

		builder.append("insert into ").append(tableName).append(" (").append(", id) values (");

		int i = 0;
		for (FieldExtractor fieldExtractor : webPageExtractor.getFields()) {
			builder.append(fieldExtractor.getName()).append(",");

			List<? extends Object> fieldValue = value.get("fieldExtractor.getName()");
			if (onlyFirstElement) {
				if (fieldValue != null) {
					values.add(fieldValue.get(0));
				} else {
					values.add(null);
				}
				types[i] = Types.ARRAY;
			} else {
				if (fieldValue != null) {
					values.add(fieldValue.toArray());
				} else {
					values.add(null);
				}
				types[i] = Types.VARCHAR;
			}
			i++;
		}

		types[i] = Types.VARCHAR;
		values.add(key);
		builder.append(", ?)");

		jdbcTemplate.update(builder.toString(), values.toArray());
	}

	@Override
	public void storeAll(Map<String, Document> map) {
		map.entrySet().forEach(e -> {
			// jdbcTemplate.update(insertQuery, e.getKey(), new
			// SqlLobValue(output.getBuffer(), lobHandler));
			});

	}

	@Override
	public void delete(String key) {
		jdbcTemplate.update(deleteQuery + " " + whereClause + " ('" + key + "')");
	}

	@Override
	public void deleteAll(Collection<String> keys) {
		jdbcTemplate.update(deleteQuery + " " + whereClause + " ('" + StringUtils.join(keys, "','") + "')");
	}
}