package io.mandrel.data.export;

import io.mandrel.data.content.FieldExtractor;
import io.mandrel.gateway.Document;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = DelimiterSeparatedValuesExporter.class, name = "csv"), @Type(value = JsonExporter.class, name = "json") })
public interface DocumentExporter extends AbstractExporter {

	void export(Collection<Document> documents, List<FieldExtractor> fields) throws Exception;
}
