package io.mandrel.requests;

import io.mandrel.blob.Blob;
import io.mandrel.metadata.FetchMetadata;
import lombok.Data;

@Data
public class Bag<T extends FetchMetadata> {
	private T metadata;
	private Blob blob;
}
