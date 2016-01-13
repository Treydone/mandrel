package io.mandrel.common.sync;

import com.facebook.swift.codec.ThriftStruct;

import io.mandrel.common.container.Status;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ThriftStruct
public class Container {

	private long spiderId;
	private long version;
	private Status status;
}
