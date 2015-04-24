package io.mandrel.data.export;

import io.mandrel.http.WebPage;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = DelimiterSeparatedValuesExporter.class, name = "csv"), @Type(value = JsonExporter.class, name = "json") })
public interface RawExporter extends Serializable {

	void export(Stream<WebPage> documents, Writer writer) throws IOException;

	String contentType();
}
