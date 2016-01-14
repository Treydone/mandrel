/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.timeline;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class Event {

	@Getter(onMethod = @__(@ThriftField(1)))
	@Setter(onMethod = @__(@ThriftField))
	private LocalDateTime time;
	@Getter(onMethod = @__(@ThriftField(2)))
	@Setter(onMethod = @__(@ThriftField))
	private String text;
	@Getter(onMethod = @__(@ThriftField(3)))
	@Setter(onMethod = @__(@ThriftField))
	private String title;
	@Getter(onMethod = @__(@ThriftField(4)))
	@Setter(onMethod = @__(@ThriftField))
	private EventType type;
	@Getter(onMethod = @__(@ThriftField(5)))
	@Setter(onMethod = @__(@ThriftField))
	private SpiderInfo spider;
	@Getter(onMethod = @__(@ThriftField(6)))
	@Setter(onMethod = @__(@ThriftField))
	private NodeInfo node;

	public enum EventType {
		SPIDER, NODE
	}

	public static Event forSpider() {
		Event event = new Event();
		event.setTime(LocalDateTime.now());
		event.setType(EventType.SPIDER);
		event.setSpider(new SpiderInfo());
		return event;
	}

	public static Event forNode() {
		Event event = new Event();
		event.setTime(LocalDateTime.now());
		event.setType(EventType.NODE);
		event.setNode(new NodeInfo());
		return event;
	}

	@Data
	@ThriftStruct
	public static class SpiderInfo {

		@Getter(onMethod = @__(@ThriftField(1)))
		@Setter(onMethod = @__(@ThriftField))
		private SpiderEventType type;
		@Getter(onMethod = @__(@ThriftField(2)))
		@Setter(onMethod = @__(@ThriftField))
		private long spiderId;
		@Getter(onMethod = @__(@ThriftField(3)))
		@Setter(onMethod = @__(@ThriftField))
		private String spiderName;

		public enum SpiderEventType {
			SPIDER_CREATED, SPIDER_STARTED, SPIDER_PAUSED, SPIDER_UPDATED, SPIDER_ENDED, SPIDER_KILLED, SPIDER_DELETED
		}

	}

	@Data
	@ThriftStruct
	public static class NodeInfo {

		@Getter(onMethod = @__(@ThriftField(1)))
		@Setter(onMethod = @__(@ThriftField))
		private NodeEventType type;
		@Getter(onMethod = @__(@ThriftField(2)))
		@Setter(onMethod = @__(@ThriftField))
		private String nodeId;

		public enum NodeEventType {
			NODE_STARTED, NODE_STOPPED
		}
	}
}
