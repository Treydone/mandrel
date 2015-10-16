package io.mandrel.frontier.store;

import io.mandrel.common.loader.NamedComponent;

import java.net.URI;

public interface FrontierStore extends NamedComponent {

	Queue<URI> queue(String name);
	
	void create(String defaultQueue);

	void finish(URI uri);

	void delete(URI uri);
}
