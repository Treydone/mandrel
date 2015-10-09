package io.mandrel.blob;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.mandrel.io.Payloads.newPayload;
import io.mandrel.io.Payload;

import java.io.File;
import java.io.InputStream;

import lombok.Data;
import lombok.experimental.Accessors;

import com.google.common.io.ByteSource;

@Data
@Accessors(fluent = true, chain = true)
public class Blob {

	private Payload payload;

	private final BlobMetadata metadata;

	public Blob payload(Payload data) {
		if (this.payload != null)
			payload.release();
		this.payload = checkNotNull(data, "data");
		return this;
	}

	public Blob payload(InputStream data) {
		return payload(newPayload(checkNotNull(data, "data")));
	}

	public Blob payload(byte[] data) {
		return payload(newPayload(checkNotNull(data, "data")));
	}

	public Blob payload(String data) {
		return payload(newPayload(checkNotNull(data, "data")));
	}

	public Blob payload(File data) {
		return payload(newPayload(checkNotNull(data, "data")));
	}

	public Blob payload(ByteSource data) {
		return payload(newPayload(checkNotNull(data, "data")));
	}
}
