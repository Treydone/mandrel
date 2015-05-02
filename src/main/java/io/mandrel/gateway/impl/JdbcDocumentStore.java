package io.mandrel.gateway.impl;

import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.gateway.Document;
import io.mandrel.gateway.DocumentStore;

import java.util.List;
import java.util.stream.Stream;

import javax.sql.DataSource;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.hazelcast.core.HazelcastInstance;

@Data
public class JdbcDocumentStore implements DocumentStore {

	private static final long serialVersionUID = 5608990195947997882L;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private transient HazelcastInstance hazelcastInstance;

	@JsonUnwrapped
	private PoolConfiguration configuration;

	private String insertQuery;

	private JdbcTemplate jdbcTemplate;

	@Override
	public void save(long spiderId, Document data) {
		jdbcTemplate.update(insertQuery, data);
	}

	@Override
	public void save(long spiderId, List<Document> data) {
		jdbcTemplate.batchUpdate(insertQuery, (List<Object[]>) null);
	}

	@Override
	public void init(WebPageExtractor webPageExtractor) {
		DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource(configuration);
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public boolean check() {
		return false;
	}

	@Override
	public void deleteAllFor(long spiderId) {
		// TODO Auto-generated method stub

	}

	@Override
	public Stream<Document> all(long spiderId) {
		// TODO Auto-generated method stub
		return null;
	}
}
