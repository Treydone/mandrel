package io.mandrel.data.export;

import io.mandrel.data.content.FieldExtractor;
import io.mandrel.gateway.Document;
import io.mandrel.http.WebPage;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;

import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Data
public class JsonExporter implements DocumentExporter, RawExporter {

	private static final long serialVersionUID = -410119107553820985L;

	@Override
	public void export(Stream<Document> documents, List<FieldExtractor> fields, Writer writer) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		ArrayNode arrayNode = mapper.createArrayNode();

		List<String> headers = fields.stream().map(field -> field.getName()).collect(Collectors.toList());

		documents.forEach(doc -> {
			ObjectNode rootNode = arrayNode.objectNode();
			for (String header : headers) {
				List<? extends Object> values = doc.get(header);
				if (!CollectionUtils.isEmpty(values)) {
					rootNode.putPOJO(header, values);
				}
			}
		});

		mapper.writeValue(writer, arrayNode);
	}

	@Override
	public String contentType() {
		return "application/json";
	}

	@Override
	public void export(Stream<WebPage> pages, Writer writer) throws IOException {
		// TODO Auto-generated method stub

	}
}
