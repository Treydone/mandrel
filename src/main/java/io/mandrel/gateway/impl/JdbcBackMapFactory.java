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
public class JdbcBackMapFactory implements MapStoreFactory<String, WebPage> {

	@Override
	public MapLoader<String, WebPage> newMapStore(String mapName, Properties properties) {
		Map<String, String> configuration = properties.entrySet().stream()
				.collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue().toString()));

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

		jdbcTemplate.update(MessageFormat.format(properties.getProperty("create_query"), tableName));

		JdbcBackedMap backedMap = new JdbcBackedMap(jdbcTemplate, tableName, MessageFormat.format(properties.getProperty("insert_query"), tableName),
				MessageFormat.format(properties.getProperty("select_key_query"), tableName), MessageFormat.format(properties.getProperty("select_query"),
						tableName), MessageFormat.format(properties.getProperty("delete_query"), tableName), properties.getProperty("where_clause"),
				properties.getProperty("paging"));
		return backedMap;
	}
}