package io.mandrel.common.store.impl;

import io.mandrel.common.content.WebPageExtractor;
import io.mandrel.common.store.Document;
import io.mandrel.common.store.DocumentStore;

import java.util.List;

import javax.sql.DataSource;

import lombok.Data;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

@Data
public class JdbcDocumentStore implements DocumentStore {

	@JsonUnwrapped
	private PoolConfiguration configuration;

	private String insertQuery;

	private JdbcTemplate jdbcTemplate;

	@Override
	public void save(Document data) {
		jdbcTemplate.update(insertQuery, data);
	}

	@Override
	public void save(List<Document> data) {
		jdbcTemplate.batchUpdate(insertQuery, (List<Object[]>) null);
	}

	@Override
	public void init(WebPageExtractor webPageExtractor) {
		DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource(
				configuration);
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public boolean check() {
		return false;
	}
}
