package io.mandrel.gateway.impl;

import io.mandrel.http.WebPage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;

@Data
public class JdbcBackedMap implements MapStore<String, WebPage>, MapLoader<String, WebPage> {

	private final JdbcTemplate jdbcTemplate;

	private final String tableName;

	private final String insertQuery;

	private final String selectKeyQuery;

	private final String selectQuery;

	private final String deleteQuery;

	private final String whereClause;

	private final String paging;

	private KryoFactory factory = new KryoFactory() {
		public Kryo create() {
			Kryo kryo = new Kryo();
			kryo.register(WebPage.class);
			return kryo;
		}
	};

	private KryoPool pool = new KryoPool.Builder(factory).softReferences().build();

	@Override
	public WebPage load(String key) {
		Kryo kryo = pool.borrow();
		try {
			return jdbcTemplate.queryForObject(selectKeyQuery + " " + whereClause + " ('" + key + "')",
					(row, nb) -> kryo.readObject(new Input(row.getBlob(2).getBinaryStream()), WebPage.class));
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		} finally {
			pool.release(kryo);
		}
	}

	@Override
	public Map<String, WebPage> loadAll(Collection<String> keys) {
		Kryo kryo = pool.borrow();
		try {
			List<WebPage> results = jdbcTemplate.query(selectQuery + " " + whereClause + " ('" + StringUtils.join(keys, "','") + "')",
					(row, nb) -> kryo.readObject(new Input(row.getBlob(2).getBinaryStream()), WebPage.class));
			if (results != null) {
				return results.stream().collect(Collectors.toMap(w -> w.getUrl().toString(), w -> w));
			}
			return null;
		} finally {
			pool.release(kryo);
		}
	}

	@Override
	public Set<String> loadAllKeys() {
		Kryo kryo = pool.borrow();
		try {
			List<WebPage> results = jdbcTemplate.query(selectQuery,
					(row, nb) -> kryo.readObject(new Input(row.getBlob(2).getBinaryStream()), WebPage.class));
			if (results != null) {
				return results.stream().map(w -> w.getUrl().toString()).collect(Collectors.toSet());
			}
			return null;
		} finally {
			pool.release(kryo);
		}
	}

	@Override
	public void store(String key, WebPage value) {
		Kryo kryo = pool.borrow();
		try {
			LobHandler lobHandler = new DefaultLobHandler();
			Output output = new Output(1024);
			kryo.writeObject(output, value);
			jdbcTemplate.update(insertQuery, key, new SqlLobValue(output.getBuffer(), lobHandler));
		} finally {
			pool.release(kryo);
		}
	}

	@Override
	public void storeAll(Map<String, WebPage> map) {
		Kryo kryo = pool.borrow();
		try {
			map.entrySet().forEach(e -> {
				LobHandler lobHandler = new DefaultLobHandler();
				Output output = new Output(1024);
				kryo.writeObject(output, e.getValue());
				jdbcTemplate.update(insertQuery, e.getKey(), new SqlLobValue(output.getBuffer(), lobHandler));
			});
		} finally {
			pool.release(kryo);
		}
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