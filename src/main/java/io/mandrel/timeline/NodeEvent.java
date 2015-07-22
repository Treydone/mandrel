package io.mandrel.timeline;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class NodeEvent extends Event {

	private static final long serialVersionUID = -4972913518366097298L;

	private NodeEventType type;

	private String nodeId;

	public enum NodeEventType {
		NODE_STARTED, NODE_STOPPED
	}
}
