package io.mandrel.endpoints.rest;

import io.mandrel.common.data.Spider;
import io.mandrel.data.export.DocumentExporter;
import io.mandrel.data.export.ExporterService;
import io.mandrel.data.export.RawExporter;
import io.mandrel.data.spider.Analysis;
import io.mandrel.data.spider.SpiderService;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/spiders")
@RequestMapping(value = "/spiders", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class SpiderResource {

	private final SpiderService spiderService;

	private final ExporterService exporterService;

	@Autowired
	public SpiderResource(SpiderService spiderService, ExporterService exporterService) {
		this.spiderService = spiderService;
		this.exporterService = exporterService;
	}

	@ApiOperation(value = "List all the spiders", response = Spider.class, responseContainer = "List")
	@RequestMapping
	public List<Spider> all() {
		return spiderService.list().collect(Collectors.toList());
	}

	@ApiOperation(value = "Add a spider")
	@RequestMapping(method = RequestMethod.POST)
	public Spider add(@RequestParam List<String> urls) {
		return spiderService.add(urls);
	}

	@ApiOperation(value = "Add a spider")
	@RequestMapping(method = RequestMethod.POST)
	public Spider add(Spider spider) {
		return spiderService.add(spider);
	}

	@ApiOperation(value = "Update a spider", response = Spider.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public Spider update(@PathVariable Long id, Spider spider) {
		return spiderService.update(spider);
	}

	@ApiOperation(value = "Find a spider by its id", response = Spider.class)
	@RequestMapping(value = "/{id}")
	public Spider id(@PathVariable Long id) {
		return spiderService.get(id).map(opt -> opt).orElse(null);
	}

	@ApiOperation(value = "Start a spider")
	@RequestMapping(value = "/{id}/start")
	public void start(@PathVariable Long id) {
		spiderService.get(id).ifPresent(opt -> spiderService.start(id));
	}

	@ApiOperation(value = "Analyze a source against a spider")
	@RequestMapping(value = "/{id}/analyze")
	public Analysis analyze(@PathVariable Long id, @RequestParam String source) {
		return spiderService.analyze(id, source);
	}

	@ApiOperation(value = "Pause a spider")
	@RequestMapping(value = "/{id}/pause")
	public void pause(@PathVariable Long id) {
		// TODO
	}

	@ApiOperation(value = "Cancel a spider")
	@RequestMapping(value = "/{id}/cancel")
	public void cancel(@PathVariable Long id) {
		spiderService.get(id).ifPresent(opt -> spiderService.cancel(id));
	}

	@ApiOperation(value = "Delete a spider")
	@RequestMapping(value = "/{id}/delete", method = RequestMethod.DELETE)
	public void delete(@PathVariable Long id) {
		spiderService.get(id).ifPresent(opt -> spiderService.delete(id));
	}

	@ApiOperation(value = "Export the data of the extractor of a spider")
	@RequestMapping(value = "/{id}/export/{extractorName}", method = RequestMethod.POST)
	public void export(@PathVariable Long id, @PathVariable String extractorName, DocumentExporter exporter, HttpServletResponse response) {
		exporterService.export(id, extractorName, exporter, response);
	}

	@ApiOperation(value = "Export the raw data of a spider")
	@RequestMapping(value = "/{id}/raw/export", method = RequestMethod.POST)
	public void rawExport(@PathVariable Long id, RawExporter exporter, HttpServletResponse response) {
		exporterService.export(id, exporter, response);
	}
}
