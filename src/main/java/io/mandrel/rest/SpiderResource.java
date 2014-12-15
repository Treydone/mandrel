package io.mandrel.rest;

import io.mandrel.spider.Spider;
import io.mandrel.spider.SpiderService;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/spider")
public class SpiderResource {

	private final SpiderService spiderResource;

	@Inject
	public SpiderResource(SpiderService spiderResource) {
		this.spiderResource = spiderResource;
	}

	@Path("/all")
	public List<Spider> all() {
		return spiderResource.list().collect(Collectors.toList());
	}

	@Path("/add")
	@POST
	public void add(Spider spider) {
		spiderResource.add(spider);
	}

	@Path("/{id}")
	public Spider id(@PathParam("id") Long id) {
		return spiderResource.get(id).map(opt -> opt).orElse(null);
	}

	@Path("/{id}/pause")
	public Spider pause(@PathParam("id") Long id) {
		return spiderResource.get(id).map(opt -> opt).orElse(null);
	}

	@Path("/{id}/cancel")
	public Spider cancel(@PathParam("id") Long id) {
		return spiderResource.get(id).map(opt -> opt).orElse(null);
	}
}
