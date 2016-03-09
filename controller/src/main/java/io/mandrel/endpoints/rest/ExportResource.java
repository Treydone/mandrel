/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.endpoints.rest;

import io.mandrel.common.NotFoundException;
import io.mandrel.data.export.DelimiterSeparatedValuesExporter;
import io.mandrel.data.export.Exporter;
import io.mandrel.data.export.Exporter.ExporterDefinition;
import io.mandrel.data.export.ExporterService;
import io.mandrel.data.export.JsonExporter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Charsets;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(basePath = Apis.PREFIX, value = "/spiders")
@RequestMapping(value = Apis.PREFIX + "/spiders")
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ExportResource {

	private final ExporterService exporterService;

	@ApiOperation(value = "Export the data of the extractor of a spider using a custom exporter in the classpath")
	@RequestMapping(value = "/{id}/export/{extractorName}", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void export(@PathVariable Long id, @PathVariable String extractorName, @RequestBody ExporterDefinition<? extends Exporter> exporter,
			@RequestParam(defaultValue = "true") boolean compress, HttpServletResponse response) throws IOException {
		internalExport(id, extractorName, exporter.build(null), response, compress);
	}

	@ApiOperation(value = "Export the data of the extractor of a spider in a format specified in the parameter")
	@RequestMapping(value = "/{id}/export/{extractorName}", method = RequestMethod.GET, params = "format")
	public void export(@PathVariable Long id, @PathVariable String extractorName, @RequestParam(required = true) String format,
			@RequestParam(defaultValue = "true") boolean compress, HttpServletResponse response) throws IOException {
		Exporter exporter = null;
		if ("csv".equals(format)) {
			exporter = new DelimiterSeparatedValuesExporter();
		} else if ("json".equals(format)) {
			exporter = new JsonExporter();
		} else {
			response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return;
		}
		internalExport(id, extractorName, exporter, response, compress);
	}

	@ApiOperation(value = "Export the raw data of a spider using a custom exporter in the classpath")
	@RequestMapping(value = "/{id}/raw/export", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void rawExport(@PathVariable Long id, @RequestBody ExporterDefinition<? extends Exporter> exporter,
			@RequestParam(defaultValue = "true") boolean compress, HttpServletResponse response) throws IOException {
		internalRawExport(id, exporter.build(null), response, compress);
	}

	@ApiOperation(value = "Export the raw data of a spider in a format specified in the parameter")
	@RequestMapping(value = "/{id}/raw/export", method = RequestMethod.GET, params = "format")
	public void rawExport(@PathVariable Long id, @RequestParam(required = true) String format, @RequestParam(defaultValue = "true") boolean compress,
			HttpServletResponse response) throws IOException {
		Exporter exporter = null;
		if ("csv".equals(format)) {
			exporter = new DelimiterSeparatedValuesExporter();
		} else if ("json".equals(format)) {
			exporter = new JsonExporter();
		} else {
			response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return;
		}
		internalRawExport(id, exporter, response, compress);
	}

	protected void internalRawExport(Long id, Exporter exporter, HttpServletResponse response, boolean compress) throws IOException {
		try (OutputStreamWriter writer = prepareExport(exporter, response, compress, "export")) {
			try {
				exporterService.export(id, exporter, writer);
			} catch (NotFoundException e) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			}
		} finally {
			response.flushBuffer();
		}
	}

	protected void internalExport(Long id, String extractorName, Exporter exporter, HttpServletResponse response, boolean compress) throws IOException {
		try (OutputStreamWriter writer = prepareExport(exporter, response, compress, extractorName)) {
			try {
				exporterService.export(id, extractorName, exporter, writer);
			} catch (NotFoundException e) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			}
		} finally {
			response.flushBuffer();
		}
	}

	public OutputStreamWriter prepareExport(Exporter exporter, HttpServletResponse response, boolean compress, String name) throws IOException {
		response.setContentType(exporter.contentType());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "-" + DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())
				+ (compress ? ".gzip" : "") + "\"");

		OutputStreamWriter writer;
		if (compress) {
			response.setHeader("Content-Encoding", "gzip");
			writer = new OutputStreamWriter(new GZIPOutputStream(response.getOutputStream()), Charsets.UTF_8);
		} else {
			writer = new OutputStreamWriter(response.getOutputStream(), Charsets.UTF_8);
		}
		return writer;
	}
}
