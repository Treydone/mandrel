package io.mandrel.timeline;

import java.util.List;

public interface TimelineService {

	void add(Event event);

	List<Event> page(int from, int size);
}
