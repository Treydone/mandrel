package io.mandrel.frontier.store;

import io.mandrel.common.net.Uri;
import io.mandrel.frontier.PoolCallback;
import lombok.Data;

@Data(staticConstructor = "of")
public class FetchRequest {

	private final String topic;
	private final PoolCallback<Uri> callback;
}
