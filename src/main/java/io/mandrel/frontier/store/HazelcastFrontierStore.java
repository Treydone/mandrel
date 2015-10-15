package io.mandrel.frontier.store;

import java.net.URI;

import javax.inject.Inject;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HazelcastFrontierStore implements FrontierStore {

	private final HazelcastInstance instance;

	@Override
	public Queue<URI> queue(String name) {
		return new HazelcastQueue<>(instance.getQueue(name));
	}

	@Override
	public void create(String defaultQueue) {

	}

	@Override
	public void finish(URI uri) {

	}

	@Override
	public void delete(URI uri) {

	}

	@Data
	public static class HazelcastQueue<T> implements Queue<T> {

		private final IQueue<T> raw;

		@Override
		public T pool() {
			return raw.poll();
		}

		@Override
		public void schedule(T t) {
			raw.add(t);
		}
	}

	@Override
	public String name() {
		return "internal";
	}
}
