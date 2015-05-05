package io.mandrel.data.export;

import io.mandrel.data.content.FieldExtractor;
import io.mandrel.gateway.Document;

import java.io.Serializable;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = DelimiterSeparatedValuesExporter.class, name = "csv"), @Type(value = JsonExporter.class, name = "json") })
public interface DocumentExporter extends Serializable {

	void export(Collection<Document> documents, List<FieldExtractor> fields) throws Exception;

	String contentType();

	void init(Writer writer) throws Exception;

	void close() throws Exception;
}
