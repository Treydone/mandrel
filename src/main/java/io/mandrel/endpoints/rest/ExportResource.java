package io.mandrel.endpoints.rest;

import io.mandrel.common.NotFoundException;
import io.mandrel.data.export.DelimiterSeparatedValuesExporter;
import io.mandrel.data.export.DocumentExporter;
import io.mandrel.data.export.ExporterService;
import io.mandrel.data.export.JsonExporter;
import io.mandrel.data.export.RawExporter;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/spiders")
@RequestMapping(value = "/spiders")
@RestController
public class ExportResource {

	private final ExporterService exporterService;

	@Autowired
	public ExportResource(ExporterService exporterService) {
		this.exporterService = exporterService;
	}

	@ApiOperation(value = "Export the data of the extractor of a spider using a custom exporter in the classpath")
	@RequestMapping(value = "/{id}/export/{extractorName}", method = RequestMethod.GET)
	public void export(@PathVariable Long id, @PathVariable String extractorName, DocumentExporter exporter, HttpServletResponse response) throws IOException {
		internalExport(id, extractorName, exporter, response);
	}

	public void internalExport(Long id, String extractorName, DocumentExporter exporter, HttpServletResponse response) throws IOException {
		response.setContentType(exporter.contentType());
		try {
			exporterService.export(id, extractorName, exporter, response.getWriter());
		} catch (NotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@ApiOperation(value = "Export the data of the extractor of a spider in a format specified in the parameter")
	@RequestMapping(value = "/{id}/export/{extractorName}", method = RequestMethod.GET, params = "format")
	public void export(@PathVariable Long id, @PathVariable String extractorName, @RequestParam(required = true) String format, HttpServletResponse response)
			throws IOException {
		DocumentExporter exporter = null;
		if ("csv".equals(format)) {
			exporter = new DelimiterSeparatedValuesExporter();
		} else if ("json".equals(format)) {
			exporter = new JsonExporter();
		} else {
			response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return;
		}
		internalExport(id, extractorName, exporter, response);
	}

	@ApiOperation(value = "Export the raw data of a spider using a custom exporter in the classpath")
	@RequestMapping(value = "/{id}/raw/export", method = RequestMethod.GET)
	public void rawExport(@PathVariable Long id, RawExporter exporter, HttpServletResponse response) throws IOException {
		internalRawExport(id, exporter, response);
	}

	public void internalRawExport(Long id, RawExporter exporter, HttpServletResponse response) throws IOException {
		response.setContentType(exporter.contentType());
		try {
			exporterService.export(id, exporter, response.getWriter());
		} catch (NotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@ApiOperation(value = "Export the raw data of a spider in a format specified in the parameter")
	@RequestMapping(value = "/{id}/raw/export", method = RequestMethod.GET, params = "format")
	public void rawExport(@PathVariable Long id, @RequestParam(required = true) String format, HttpServletResponse response) throws IOException {
		RawExporter exporter = null;
		if ("csv".equals(format)) {
			exporter = new DelimiterSeparatedValuesExporter();
		} else if ("json".equals(format)) {
			exporter = new JsonExporter();
		} else {
			response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return;
		}
		internalRawExport(id, exporter, response);
	}
}
