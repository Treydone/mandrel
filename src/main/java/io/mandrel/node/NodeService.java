package io.mandrel.node;

import io.mandrel.monitor.SigarService;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapConfig.EvictionPolicy;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Component
@Slf4j
public class NodeService {

	private static final String NODES = "nodes";

	private final ScheduledExecutorService scheduledExecutorService;

	private final SigarService sigarService;

	private final HazelcastInstance instance;

	@Inject
	public NodeService(ScheduledExecutorService scheduledExecutorService, SigarService sigarService, HazelcastInstance instance) {
		this.scheduledExecutorService = scheduledExecutorService;
		this.sigarService = sigarService;
		this.instance = instance;
	}

	@PostConstruct
	public void init() {
		if (!instance.getConfig().getMapConfigs().containsKey(NODES)) {
			MapConfig mapConfig = new MapConfig();
			mapConfig.setEvictionPolicy(EvictionPolicy.LRU);
			mapConfig.setMaxIdleSeconds(20);
			mapConfig.setName(NODES);
			instance.getConfig().addMapConfig(mapConfig);
		}

		scheduledExecutorService.scheduleAtFixedRate(() -> {
			try {
				Map<String, Object> infos = sigarService.infos();
				String uuid = instance.getLocalEndpoint().getUuid();

				Node dhis = new Node();
				dhis.setInfos(infos);
				dhis.setUuid(uuid);
				_nodes().put(uuid, dhis);
			} catch (Exception e) {
				log.warn("Can not set the infos for the endpoint", e);
			}
		}, 0, 1000, TimeUnit.MILLISECONDS);
	}

	public Node node(String id) {
		return _nodes().get(id);
	}

	public Map<String, Node> nodes(Collection<String> uuids) {
		return _nodes().entrySet().stream().filter(idNode -> uuids.contains(idNode.getKey()))
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
	}

	public Map<String, Node> nodes() {
		return _nodes();
	}

	private IMap<String, Node> _nodes() {
		return instance.<String, Node> getMap(NODES);
	}

}
