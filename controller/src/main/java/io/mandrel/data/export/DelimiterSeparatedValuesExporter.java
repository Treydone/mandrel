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
package io.mandrel.data.export;

import io.mandrel.blob.Blob;
import io.mandrel.common.service.TaskContext;
import io.mandrel.data.content.FieldExtractor;
import io.mandrel.document.Document;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Slf4j
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class DelimiterSeparatedValuesExporter implements Exporter {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class DelimiterSeparatedValuesExporterDefinition extends ExporterDefinition<DelimiterSeparatedValuesExporter> {

		private static final long serialVersionUID = 252972137729111484L;

		@JsonProperty("quote_char")
		private char quoteChar = '"';

		@JsonProperty("delimiter_values")
		private char delimiterValuesChar = ',';

		@JsonProperty("delimiter_multivalues")
		private char delimiterMultiValuesChar = '|';

		@JsonProperty("keep_only_first_value")
		private boolean keepOnlyFirstValue = false;

		@JsonProperty("add_header")
		private boolean addHeader = true;

		@JsonProperty("end_of_line_symbols")
		private String endOfLineSymbols = "\r\n";

		@Override
		public DelimiterSeparatedValuesExporter build(TaskContext context) {
			return new DelimiterSeparatedValuesExporter().addHeader(addHeader).delimiterMultiValuesChar(delimiterMultiValuesChar)
					.delimiterValuesChar(delimiterValuesChar).endOfLineSymbols(endOfLineSymbols).keepOnlyFirstValue(keepOnlyFirstValue).quoteChar(quoteChar);
		}

		@Override
		public String name() {
			return "csv";
		}
	}

	private char quoteChar;
	private char delimiterValuesChar;
	private char delimiterMultiValuesChar;
	private boolean keepOnlyFirstValue;
	private boolean addHeader;
	private String endOfLineSymbols;

	private transient ICsvListWriter csvWriter;

	private transient boolean headerAdded = false;

	@Override
	public void init(Writer writer) throws Exception {
		csvWriter = new CsvListWriter(writer, new CsvPreference.Builder(quoteChar, delimiterValuesChar, endOfLineSymbols).build());
	}

	@Override
	public void close() throws Exception {
		csvWriter.flush();
		csvWriter.close();
	}

	@Override
	public void export(Collection<Document> documents, List<FieldExtractor> fields) {
		List<String> headers = fields.stream().map(field -> field.getName()).collect(Collectors.toList());
		if (addHeader && !headerAdded) {
			try {
				csvWriter.writeHeader(headers.toArray(new String[] {}));
			} catch (Exception e) {
				log.debug("Can not write header {}", csvWriter.getLineNumber(), e);
			}
			headerAdded = true;
		}

		List<String> buffer = new ArrayList<>(fields.size());

		documents.forEach(doc -> {
			for (String header : headers) {
				List<? extends Object> values = doc.get(header);
				if (!CollectionUtils.isEmpty(values)) {
					if (keepOnlyFirstValue) {
						buffer.add(values.get(0).toString());
					} else {
						buffer.add(StringUtils.join(values, delimiterMultiValuesChar));
					}
				} else {
					buffer.add(StringUtils.EMPTY);
				}
			}

			try {
				csvWriter.write(buffer);
			} catch (Exception e) {
				log.debug("Can not write line {}", csvWriter.getLineNumber(), e);
			}

			buffer.clear();
		});
	}

	@Override
	public String contentType() {
		return "text/csv; charset=UTF-8";
	}

	@Override
	public void export(Collection<Blob> blobs) {
		if (addHeader && !headerAdded) {
			try {
				csvWriter.writeHeader("url", "statusCode", "statusText", "lastCrawlDate", "outlinks", "timeToFetch");
			} catch (Exception e) {
				log.debug("Can not write header {}", csvWriter.getLineNumber(), e);
			}
			headerAdded = true;
		}

		List<Object> buffer = new ArrayList<>(6);

		blobs.forEach(page -> {
			buffer.add(page.getMetadata().getUri());
			buffer.add(page.getMetadata().getFetchMetadata().getStatusCode());
			buffer.add(page.getMetadata().getFetchMetadata().getStatusText());
			buffer.add(page.getMetadata().getFetchMetadata().getLastCrawlDate());
			buffer.add(page.getMetadata().getFetchMetadata().getOutlinks());
			buffer.add(page.getMetadata().getFetchMetadata().getTimeToFetch());

			try {
				csvWriter.write(buffer);
			} catch (Exception e) {
				log.debug("Can not write line {}", csvWriter.getLineNumber(), e);
			}

			buffer.clear();
		});
	}

}
