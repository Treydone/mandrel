package io.mandrel;

import feign.Client.Default;
import feign.Feign;
import feign.Logger.Level;
import feign.Target.EmptyTarget;
import feign.slf4j.Slf4jLogger;
import io.mandrel.common.client.SpringMvcContract;
import io.mandrel.endpoints.contracts.NodeContract;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.cloud.netflix.feign.support.SpringDecoder;
import org.springframework.cloud.netflix.feign.support.SpringEncoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

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
}
