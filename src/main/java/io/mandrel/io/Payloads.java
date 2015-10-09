package io.mandrel.io;

import static com.google.common.base.Preconditions.checkNotNull;
import io.mandrel.io.payload.ByteArrayPayload;
import io.mandrel.io.payload.ByteSourcePayload;
import io.mandrel.io.payload.FilePayload;
import io.mandrel.io.payload.InputStreamPayload;
import io.mandrel.io.payload.StringPayload;

import java.io.File;
import java.io.InputStream;

import com.google.common.io.ByteSource;

public class Payloads {
	private Payloads() {
	}

	public static Payload newPayload(Object data) {
		checkNotNull(data, "data");
		if (data instanceof Payload) {
			return (Payload) data;
		} else if (data instanceof InputStream) {
			return newInputStreamPayload((InputStream) data);
		} else if (data instanceof byte[]) {
			return newByteArrayPayload((byte[]) data);
		} else if (data instanceof ByteSource) {
			return newByteSourcePayload((ByteSource) data);
		} else if (data instanceof String) {
			return newStringPayload((String) data);
		} else if (data instanceof File) {
			return newFilePayload((File) data);
		} else {
			throw new UnsupportedOperationException("unsupported payload type: " + data.getClass());
		}
	}

	public static InputStreamPayload newInputStreamPayload(InputStream data) {
		return new InputStreamPayload(checkNotNull(data, "data"));
	}

	public static ByteArrayPayload newByteArrayPayload(byte[] data) {
		return new ByteArrayPayload(checkNotNull(data, "data"));
	}

	public static ByteSourcePayload newByteSourcePayload(ByteSource data) {
		return new ByteSourcePayload(checkNotNull(data, "data"));
	}

	public static StringPayload newStringPayload(String data) {
		return new StringPayload(checkNotNull(data, "data"));
	}

	public static FilePayload newFilePayload(File data) {
		return new FilePayload(checkNotNull(data, "data"));
	}
}
