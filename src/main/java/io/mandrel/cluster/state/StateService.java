package io.mandrel.cluster.state;

import io.mandrel.cluster.node.NodeService;
import io.mandrel.monitor.Infos;
import io.mandrel.monitor.SigarService;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Periodically update the state of this node in the state repository.
 * 
 * @author devil
 */
@Component
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Slf4j
public class StateService {

	private final ScheduledExecutorService scheduledExecutorService;

	private final SigarService sigarService;

	private final NodeService nodeService;

	@PostConstruct
	public void init() {
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			update();
		}, 0, 1000, TimeUnit.MILLISECONDS);
	}

	public void update() {
		try {
			Infos infos = sigarService.infos();
			nodeService.updateLocalNodeInfos(infos);
		} catch (Exception e) {
			log.warn("Can not set the infos for the endpoint", e);
		}
	}
}
