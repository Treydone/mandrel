package io.mandrel.common.sync;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@Data
@ThriftStruct
public class SyncRequest {

	@Getter(onMethod = @__(@ThriftField(1)))
	@Setter(onMethod = @__(@ThriftField))
	private List<byte[]> definitions;
}
