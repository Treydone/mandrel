package io.mandrel.data.content;

import java.io.Serializable;

import lombok.Data;

@Data
public class Extractor implements Serializable {
	private static final long serialVersionUID = -4747100283318025613L;

	private String type;
	private String value;
	private SourceType source;
}
