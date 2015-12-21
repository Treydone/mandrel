package io.mandrel.frontier.store;

import io.mandrel.frontier.PoolCallback;

import java.net.URI;

import lombok.Data;

@Data(staticConstructor = "of")
public class FetchRequest {

	private final String topic;
	private final PoolCallback<URI> callback;
}
