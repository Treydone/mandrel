package io.mandrel.timeline;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SpiderEvent extends Event {

	private static final long serialVersionUID = -4972913518366097298L;

	private SpiderEventType type;

	private long spiderId;

	private String spiderName;

	public enum SpiderEventType {
		SPIDER_NEW, SPIDER_STARTED, SPIDER_ENDED, SPIDER_CANCELLED
	}
}
