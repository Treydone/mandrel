package io.mandrel.rest;

import io.mandrel.common.data.Spider;
import io.mandrel.service.spider.SpiderService;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/spiders")
@RequestMapping(value = "/spiders", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class SpiderResource {

	private final SpiderService spiderService;

	@Inject
	public SpiderResource(SpiderService spiderService) {
		this.spiderService = spiderService;
	}

	@ApiOperation(value = "List all the spiders", response = Spider.class, responseContainer = "List")
	@RequestMapping
	public List<Spider> all() {
		return spiderService.list().collect(Collectors.toList());
	}

	@ApiOperation(value = "Add a spider")
	@RequestMapping(method = RequestMethod.POST)
	public void add(Spider spider) {
		spiderService.add(spider);
	}

	@ApiOperation(value = "Find a spider by its id", response = Spider.class)
	@RequestMapping(value = "/{id}")
	public Spider id(@PathVariable("id") Long id) {
		return spiderService.get(id).map(opt -> opt).orElse(null);
	}

	@ApiOperation(value = "Start a spider")
	@RequestMapping(value = "/{id}/start")
	public void start(@PathVariable("id") Long id) {
		spiderService.get(id).ifPresent(opt -> spiderService.start(id));
	}

	@ApiOperation(value = "Cancel a spider")
	@RequestMapping(value = "/{id}/cancel")
	public void cancel(@PathVariable("id") Long id) {
		spiderService.get(id).ifPresent(opt -> spiderService.cancel(id));
	}
}
