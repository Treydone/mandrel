package io.mandrel.timeline;

import java.io.Serializable;

import org.joda.time.DateTime;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class Event implements Serializable {

	private static final long serialVersionUID = -6126722656700114179L;

	private DateTime time;

	private String data;
}
