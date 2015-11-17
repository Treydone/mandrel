package io.mandrel;

import java.util.stream.Collectors;

import io.mandrel.controller.impl.MongoControllerRepository;

import org.junit.Test;

import com.mongodb.MongoClient;

public class MyTest {

	@Test
	public void test5() {

		MongoClient mongo = new MongoClient();

		MongoControllerRepository repository = new MongoControllerRepository(mongo);
		repository.init();

		repository.list().collect(Collectors.toList());

		repository.listActive().collect(Collectors.toList());

	}
}
