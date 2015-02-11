package io.mandrel.common.content;

public abstract class NamedDataExtractorFormatter {

	public abstract String getName();

	public abstract Extractor getExtractor();

	public abstract Formatter getFormatter();
}
