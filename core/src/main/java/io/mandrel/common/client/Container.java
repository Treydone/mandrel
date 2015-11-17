package io.mandrel.common.client;

import io.mandrel.common.container.Status;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Container {

	private long spiderId;

	private long version;

	private Status status;
}
