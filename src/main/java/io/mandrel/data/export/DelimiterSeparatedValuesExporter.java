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

import io.mandrel.data.content.FieldExtractor;
import io.mandrel.document.Document;
import io.mandrel.metadata.FetchMetadata;
import io.mandrel.requests.Bag;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Slf4j
public class DelimiterSeparatedValuesExporter implements DocumentExporter, RawExporter {

	private static final long serialVersionUID = -7085997792228493889L;

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
	public void export(Collection<Bag<? extends FetchMetadata>> documents) {
		if (addHeader && !headerAdded) {
			try {
				csvWriter.writeHeader("url", "statusCode", "statusText", "lastCrawlDate", "outlinks", "headers");
			} catch (Exception e) {
				log.debug("Can not write header {}", csvWriter.getLineNumber(), e);
			}
			headerAdded = true;
		}

		List<Object> buffer = new ArrayList<>(6);

		documents.forEach(page -> {
			buffer.add(page.getMetadata().getUri());
			buffer.add(page.getMetadata().getStatusCode());
			buffer.add(page.getMetadata().getStatusText());
			buffer.add(page.getMetadata().getLastCrawlDate());
			buffer.add(page.getMetadata().getOutlinks());

			try {
				csvWriter.write(buffer);
			} catch (Exception e) {
				log.debug("Can not write line {}", csvWriter.getLineNumber(), e);
			}

			buffer.clear();
		});
	}

	@Override
	public String getType() {
		return "csv";
	}
}
