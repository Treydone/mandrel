package io.mandrel.rest;

import io.mandrel.spider.Spider;
import io.mandrel.spider.SpiderService;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/spider")
@Path("/spider")
@Produces(MediaType.APPLICATION_JSON)
@Resource
@Slf4j
public class SpiderResource {

	private final SpiderService spiderResource;

	@Inject
	public SpiderResource(SpiderService spiderResource) {
		log.debug("Starting spider resource...");
		this.spiderResource = spiderResource;
	}

	@ApiOperation(value = "List all the spiders", response = Spider.class, responseContainer = "List")
	@Path("/all")
	public List<Spider> all() {
		return spiderResource.list().collect(Collectors.toList());
	}

	@ApiOperation(value = "Add a spider")
	@Path("/add")
	@POST
	public void add(Spider spider) {
		spiderResource.add(spider);
	}

	@ApiOperation(value = "Find a spider by its id", response = Spider.class)
	@Path("/{id}")
	public Spider id(@PathParam("id") Long id) {
		return spiderResource.get(id).map(opt -> opt).orElse(null);
	}

	@ApiOperation(value = "Pause a spider", response = Spider.class)
	@Path("/{id}/pause")
	public Spider pause(@PathParam("id") Long id) {
		return spiderResource.get(id).map(opt -> opt).orElse(null);
	}

	@ApiOperation(value = "Cancel a spider", response = Spider.class)
	@Path("/{id}/cancel")
	public Spider cancel(@PathParam("id") Long id) {
		return spiderResource.get(id).map(opt -> opt).orElse(null);
	}
}
