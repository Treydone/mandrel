package io.mandrel.timeline;

import io.mandrel.messaging.StompService;

import java.util.List;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HazelcastTimelineService implements TimelineService {

	private final HazelcastInstance instance;
	
	private final StompService stompService;

	@Override
	public void add(Event event) {
		instance.getList("timeline").add(event);
		stompService.publish(event);
	}

	@Override
	public List<Event> page(int from, int size) {
		IList<Event> timeline = instance.getList("timeline");
		int total = timeline.size();

		if (from > total) {
			return null;
		}
		return timeline.subList(from, Math.min(size, total));
	}
}
