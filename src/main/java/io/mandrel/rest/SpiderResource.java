package io.mandrel.rest;

import io.mandrel.service.spider.Spider;
import io.mandrel.service.spider.SpiderService;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/spider")
@Path("/spider")
@Produces(MediaType.APPLICATION_JSON)
@Component
@Slf4j
public class SpiderResource {

	private final SpiderService spiderService;

	@Inject
	public SpiderResource(SpiderService spiderService) {
		log.debug("Starting spider Service...");
		this.spiderService = spiderService;
	}

	@ApiOperation(value = "List all the spiders", response = Spider.class, responseContainer = "List")
	@Path("/all")
	@GET
	public List<Spider> all() {
		return spiderService.list().collect(Collectors.toList());
	}

	@ApiOperation(value = "Add a spider")
	@Path("/add")
	@POST
	public Spider add(Spider spider) {
		return spiderService.add(spider);
	}

	@ApiOperation(value = "Find a spider by its id", response = Spider.class)
	@Path("/{id}")
	@GET
	public Spider id(@PathParam("id") Long id) {
		return spiderService.get(id).map(opt -> opt).orElse(null);
	}

	@ApiOperation(value = "Start a spider", response = Spider.class)
	@Path("/{id}/start")
	@GET
	public Spider start(@PathParam("id") Long id) {
		Spider spider = spiderService.get(id).map(opt -> {
			spiderService.start(opt);
			return opt;
		}).orElse(null);
		return spider;
	}

	@ApiOperation(value = "Pause a spider", response = Spider.class)
	@Path("/{id}/pause")
	@GET
	public Spider pause(@PathParam("id") Long id) {
		return spiderService.get(id).map(opt -> opt).orElse(null);
	}

	@ApiOperation(value = "Cancel a spider", response = Spider.class)
	@Path("/{id}/cancel")
	@GET
	public Spider cancel(@PathParam("id") Long id) {
		return spiderService.get(id).map(opt -> opt).orElse(null);
	}
}
