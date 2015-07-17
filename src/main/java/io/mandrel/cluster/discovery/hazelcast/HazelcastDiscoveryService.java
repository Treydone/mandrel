package io.mandrel.cluster.discovery.hazelcast;

import io.mandrel.cluster.discovery.DiscoveryService;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HazelcastDiscoveryService implements DiscoveryService {

	private final HazelcastInstance hazelcastInstance;

	public List<String> all() {
		return hazelcastInstance.getCluster().getMembers().stream().map(member -> member.getUuid()).collect(Collectors.toList());
	}

	public String dhis() {
		return hazelcastInstance.getCluster().getLocalMember().getUuid();
	}
}
