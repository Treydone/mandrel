package io.mandrel.common.sync;

import java.util.List;

import com.facebook.swift.codec.ThriftStruct;

import lombok.Data;

@Data
@ThriftStruct
public class SyncRequest {

	private List<byte[]> definitions;
}
