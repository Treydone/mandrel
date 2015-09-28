package io.mandrel.requests;

import lombok.Data;

@Data
public class Bag<T extends Metadata> {
	private T metadata;
	private Blob blob;
}
