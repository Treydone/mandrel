package io.mandrel.cluster.instance;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

@Component
public class StateService implements ApplicationListener<ContextStartedEvent> {

	private final AtomicBoolean started = new AtomicBoolean();

	@Override
	public void onApplicationEvent(ContextStartedEvent contextStartedEvent) {
		started.set(true);
	}

	public boolean isStarted() {
		return started.get();
	}

	@PreDestroy
	public void destroy() {
		started.set(false);
	}
}
