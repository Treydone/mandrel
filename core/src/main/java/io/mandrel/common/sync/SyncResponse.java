package io.mandrel.common.sync;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SyncResponse {

	private List<Long> created;
	private List<Long> updated;
	private List<Long> deleted;
}
