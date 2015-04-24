package io.mandrel.data.export;

import io.mandrel.data.content.FieldExtractor;
import io.mandrel.gateway.Document;
import io.mandrel.http.WebPage;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private int delimiterValuesChar = ',';

	@JsonProperty("delimiter_multivalues")
	private int delimiterMultiValuesChar = '|';

	@JsonProperty("keep_only_first_value")
	private boolean keepOnlyFirstValue = false;

	@JsonProperty("add_header")
	private boolean addHeader = true;

	@JsonProperty("end_of_line_symbols")
	private String endOfLineSymbols = "\r\n";

	@Override
	public void export(Stream<Document> documents, List<FieldExtractor> fields, Writer writer) throws IOException {

		try (ICsvListWriter csvWriter = new CsvListWriter(writer, new CsvPreference.Builder(quoteChar, delimiterValuesChar, endOfLineSymbols).build())) {

			List<String> headers = fields.stream().map(field -> field.getName()).collect(Collectors.toList());
			if (addHeader) {
				csvWriter.writeHeader(headers.toArray(new String[] {}));
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
	}

	@Override
	public String contentType() {
		return "text/csv";
	}

	@Override
	public void export(Stream<WebPage> pages, Writer writer) throws IOException {
		try (ICsvListWriter csvWriter = new CsvListWriter(writer, new CsvPreference.Builder(quoteChar, delimiterValuesChar, endOfLineSymbols).build())) {

			// TODO size?
			List<Object> buffer = new ArrayList<>();

			pages.forEach(page -> {
				buffer.add(page.getUrl());
				buffer.add(page.getMetadata().getStatusCode());
				buffer.add(page.getMetadata().getLastCrawlDate());

				try {
					csvWriter.write(buffer);
				} catch (Exception e) {
					log.debug("Can not write line {}", csvWriter.getLineNumber(), e);
				}

				buffer.clear();
			});
		}

	}
}
