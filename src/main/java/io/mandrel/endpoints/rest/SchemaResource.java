package io.mandrel.endpoints.rest;

import io.mandrel.common.data.Client;
import io.mandrel.common.schema.SchemaGenerator;
import io.mandrel.data.content.OutlinkExtractor;
import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.data.source.Source;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/schemas")
@RequestMapping(value = Apis.PREFIX + "/schemas", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SchemaResource {

	private final SchemaGenerator schemaGenerator;

	@ApiOperation(value = "Get 'source' schema")
	@RequestMapping(value = "/source", method = RequestMethod.GET)
	public ObjectNode source() throws JsonMappingException {
		return schemaGenerator.getSchema(Source.class);
	}

	@ApiOperation(value = "Get 'page extractor' schema")
	@RequestMapping(value = "/page", method = RequestMethod.GET)
	public ObjectNode page() throws JsonMappingException {
		return schemaGenerator.getSchema(WebPageExtractor.class);
	}

	@ApiOperation(value = "Get 'page outlink' schema")
	@RequestMapping(value = "/outlink", method = RequestMethod.GET)
	public ObjectNode outlink() throws JsonMappingException {
		return schemaGenerator.getSchema(OutlinkExtractor.class);
	}

	@ApiOperation(value = "Get 'client' schema")
	@RequestMapping(value = "/client", method = RequestMethod.GET)
	public ObjectNode client() throws JsonMappingException {
		return schemaGenerator.getSchema(Client.class);
	}

}
