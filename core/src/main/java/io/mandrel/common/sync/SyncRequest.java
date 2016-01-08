package io.mandrel.common.sync;

import java.util.List;

import lombok.Data;

@Data
public class SyncRequest {

	private List<byte[]> definitions;
}
