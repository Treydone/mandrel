package io.mandrel.cluster.idgenerator.hazelcast;

import io.mandrel.cluster.idgenerator.IdGenerator;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

@Component
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
public class HazelcastIdGenerator implements IdGenerator {

	private final HazelcastInstance instance;

	@Override
	public long generateId(String name) {
		return instance.getIdGenerator(name).newId();
	}
}
