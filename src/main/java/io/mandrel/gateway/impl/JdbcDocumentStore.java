package io.mandrel.gateway.impl;

import io.mandrel.data.content.FieldExtractor;
import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.gateway.Document;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.core.IMap;

@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class JdbcDocumentStore extends InternalDocumentStore {

	private static final long serialVersionUID = 5608990195947997882L;

	private Map<String, String> properties = new HashMap<>();

	@JsonProperty("table_name")
	private String tableName;

	@JsonProperty("create_query")
	private String createQuery = null;

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
	public void init(WebPageExtractor webPageExtractor) {
		super.init(webPageExtractor);

		if (createQuery == null) {
			StringBuilder builder = new StringBuilder();
			builder.append("create table if not exists ").append(tableName).append(" (");

			for (FieldExtractor fieldExtractor : extractor.getFields()) {
				builder.append(fieldExtractor.getName()).append(" text, ");
			}

			builder.append(", id varchar(255)");
			createQuery = builder.toString();
			log.info("Creating default table {} with schema: {}", tableName, createQuery);
		}
	}

	@Override
	public IMap<String, Document> getDataMap(long spiderId) {
		String mapKey = "documentstore-" + spiderId + "-" + extractor.getName();
		prepare(mapKey);
		return hazelcastInstance.<String, Document> getMap(mapKey);
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

}
