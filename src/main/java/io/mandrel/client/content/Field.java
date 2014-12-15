package io.mandrel.client.content;

public class Field {

	private String name;

	private FieldExtractor extractor;

	private FieldFormatter formatter;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FieldExtractor getExtractor() {
		return extractor;
	}

	public void setExtractor(FieldExtractor extractor) {
		this.extractor = extractor;
	}

	public FieldFormatter getFormatter() {
		return formatter;
	}

	public void setFormatter(FieldFormatter formatter) {
		this.formatter = formatter;
	}

}
