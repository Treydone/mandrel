package io.mandrel.endpoints.rest;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/")
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class HomeResource {

	@ApiOperation(value = "Home")
	@RequestMapping(method = RequestMethod.GET)
	public Map<String, Object> all() {
		return Collections.singletonMap("ok", "isOk");
	}
}
