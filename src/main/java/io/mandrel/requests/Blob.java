package io.mandrel.requests;

import lombok.Data;

@Data
public class Blob {

	private long crc32;
	private byte[] bytes;

}
