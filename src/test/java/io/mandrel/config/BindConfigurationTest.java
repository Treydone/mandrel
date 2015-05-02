package io.mandrel.config;

import java.time.LocalDateTime;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = BindConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class BindConfigurationTest {

	@Inject
	private ObjectMapper mapper;

	@Test
	public void test() throws JsonProcessingException {

		System.err.println(mapper.writeValueAsString(LocalDateTime.now()));
	}
}
