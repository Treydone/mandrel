package io.mandrel.data.content;

import java.io.Serializable;

public abstract class NamedDataExtractorFormatter implements Serializable {

	private static final long serialVersionUID = 5137211741952328647L;

	public abstract String getName();

	public abstract Extractor getExtractor();

	public abstract Formatter getFormatter();
}
