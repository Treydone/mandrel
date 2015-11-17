package io.mandrel;

import static org.junit.Assert.assertEquals;
import feign.Client.Default;
import feign.Feign;
import feign.Logger.Level;
import feign.Target.EmptyTarget;
import feign.slf4j.Slf4jLogger;
import io.mandrel.cluster.node.Node;
import io.mandrel.common.client.SpringMvcContract;
import io.mandrel.config.BindConfiguration;
import io.mandrel.endpoints.contracts.AdminContract;
import io.mandrel.endpoints.contracts.NodeContract;
import io.mandrel.monitor.SigarService;
import io.mandrel.timeline.Event;
import io.mandrel.timeline.NodeEvent;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.cloud.netflix.feign.support.SpringDecoder;
import org.springframework.cloud.netflix.feign.support.SpringEncoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.CustomLongDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class FeignTest {

	@Test
	public void test() {

		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new MappingJackson2HttpMessageConverter());
		ObjectFactory<HttpMessageConverters> convert = new ObjectFactory<HttpMessageConverters>() {
			@Override
			public HttpMessageConverters getObject() throws BeansException {
				return new HttpMessageConverters(converters);
			}
		};

		NodeContract target = Feign.builder().client(new Default(null, null)).logger(new Slf4jLogger()).logLevel(Level.FULL).contract(new SpringMvcContract())
				.encoder(new SpringEncoder(convert)).decoder(new SpringDecoder(convert)).target(EmptyTarget.create(NodeContract.class));

		System.err.println(target.dhis(URI.create("http://192.168.1.43:8080")));
	}

	@Test
	public void test2() throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		BindConfiguration.configure(mapper);

		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new MappingJackson2HttpMessageConverter(mapper));
		ObjectFactory<HttpMessageConverters> convert = new ObjectFactory<HttpMessageConverters>() {
			@Override
			public HttpMessageConverters getObject() throws BeansException {
				return new HttpMessageConverters(converters);
			}
		};

		AdminContract target = Feign.builder().client(new Default(null, null)).logger(new Slf4jLogger()).logLevel(Level.FULL).contract(new SpringMvcContract())
				.encoder(new SpringEncoder(convert)).decoder(new SpringDecoder(convert)).target(EmptyTarget.create(AdminContract.class));

		Event event = new NodeEvent().setTime(LocalDateTime.now()).setText("test");

		System.err.println(mapper.writeValueAsString(event));
		target.addEvent(event, URI.create("http://localhost:8080"));
	}

	@Test
	public void test3() throws JsonParseException, JsonMappingException, IOException {

		Event event = new NodeEvent().setTime(LocalDateTime.now()).setText("test");

		String text = "{\"@ref\":\"io.mandrel.timeline.NodeEvent\",\"time\":1447752862905,\"title\":null,\"text\":\"test\",\"type\":null,\"nodeId\":null}";

		ObjectMapper mapper = new ObjectMapper();
		BindConfiguration.configure(mapper);

		Event res = mapper.readValue(text, Event.class);
		assertEquals(res, event);
	}

	@Test
	public void test4() throws JsonParseException, JsonMappingException, IOException {

		MongoClient mongo = new MongoClient();
		MongoCollection<Document> collection = mongo.getDatabase("mandrel").getCollection("nodes");
		Document doc = collection.find(Filters.eq("_id", "aHR0cDovL2xvY2FsaG9zdDo4MDgw")).first();

		ObjectMapper objectMapper = new ObjectMapper();

		// to allow serialization of "empty" POJOs (no properties to serialize)
		// (without this setting, an exception is thrown in those cases)
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		// objectMapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
		// objectMapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
		// to write java.util.Date, Calendar as number (timestamp):
		objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// DeserializationFeature for changing how JSON is read as POJOs:

		// to prevent exception when encountering unknown property:
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// to allow coercion of JSON empty String ("") to null Object value:
		// objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

		SimpleModule module = new SimpleModule();
		module.addDeserializer(Long.class, CustomLongDeserializer.wrapperInstance);
		module.addDeserializer(long.class, CustomLongDeserializer.primitiveInstance);
		objectMapper.registerModule(module);

		// objectMapper.registerModules(new JodaModule(), new Jdk8Module(), new
		// JavaTimeModule()
		// , new AfterburnerModule()
		// );
		objectMapper.writer(new DefaultPrettyPrinter());

		System.err.println(objectMapper.writeValueAsString(new Node().setInfos(new SigarService().infos())));

		System.err.println(new String(doc.toJson(new JsonWriterSettings(JsonMode.STRICT))));
		System.err.println(objectMapper.writeValueAsString(objectMapper.readTree(doc.toJson())));

		Node res = objectMapper.readValue(doc.toJson(new JsonWriterSettings(JsonMode.STRICT)), Node.class);
		System.err.println(res);
	}
}
