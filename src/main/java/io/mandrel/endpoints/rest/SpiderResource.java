package io.mandrel.endpoints.rest;

import io.mandrel.common.data.Spider;
import io.mandrel.data.spider.Analysis;
import io.mandrel.data.spider.SpiderService;
import io.mandrel.stats.Stats;
import io.mandrel.stats.StatsService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/spiders")
@RequestMapping(value = "/spiders", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class SpiderResource {

	private final SpiderService spiderService;

	private final StatsService statsService;

	@Autowired
	public SpiderResource(SpiderService spiderService, StatsService statsService) {
		this.spiderService = spiderService;
		this.statsService = statsService;
	}

	@ApiOperation(value = "List all the spiders", response = Spider.class, responseContainer = "List")
	@RequestMapping(method = RequestMethod.GET)
	public List<Spider> all() {
		return spiderService.list().collect(Collectors.toList());
	}

	@ApiOperation(value = "Add a spider")
	@RequestMapping(method = RequestMethod.GET, params = "urls")
	public Spider add(@RequestParam List<String> urls) throws BindException {
		return spiderService.add(urls);
	}

	@ApiOperation(value = "Add a spider")
	@RequestMapping(method = RequestMethod.POST)
	public Spider add(@RequestBody Spider spider) throws BindException {
		return spiderService.add(spider);
	}

	@ApiOperation(value = "Update a spider", response = Spider.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public Spider update(@PathVariable Long id, @RequestBody Spider spider) throws BindException {
		return spiderService.update(spider);
	}

	@ApiOperation(value = "Find a spider by its id", response = Spider.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Spider id(@PathVariable Long id) {
		return spiderService.get(id).map(opt -> opt).orElse(null);
	}

	@ApiOperation(value = "Start a spider")
	@RequestMapping(value = "/{id}/start", method = RequestMethod.GET)
	public Optional<Spider> start(@PathVariable Long id) {
		return spiderService.start(id);
	}

	@ApiOperation(value = "Analyze a source against a spider")
	@RequestMapping(value = "/{id}/analyze", method = RequestMethod.GET)
	public Optional<Analysis> analyze(@PathVariable Long id, @RequestParam String source) {
		return spiderService.analyze(id, source);
	}

	@ApiOperation(value = "Pause a spider")
	@RequestMapping(value = "/{id}/pause", method = RequestMethod.GET)
	public void pause(@PathVariable Long id) {
		// TODO
	}

	@ApiOperation(value = "Cancel a spider")
	@RequestMapping(value = "/{id}/cancel", method = RequestMethod.GET)
	public Optional<Spider> cancel(@PathVariable Long id) {
		return spiderService.cancel(id);
	}

	@ApiOperation(value = "Delete a spider")
	@RequestMapping(value = "/{id}/delete", method = RequestMethod.DELETE)
	public Optional<Spider> delete(@PathVariable Long id) {
		return spiderService.delete(id);
	}

	@ApiOperation(value = "Retrieve the stats of a spider")
	@RequestMapping(value = "/{id}/stats", method = RequestMethod.GET)
	public Optional<Stats> stats(@PathVariable Long id) {
		return spiderService.get(id).map(spider -> statsService.get(spider.getId()));
	}
}
