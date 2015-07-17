package io.mandrel.data.export;

import java.io.Serializable;
import java.io.Writer;

public interface AbstractExporter extends Serializable {

	String contentType();

	void init(Writer writer) throws Exception;

	void close() throws Exception;
}
