package io.mandrel.frontier.revisit;

import io.mandrel.common.unit.TimeValue;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Interval implements Serializable {
	private static final long serialVersionUID = -4428784588678656049L;

	@JsonProperty("max_retry")
	private final int maxRetry;

	@JsonProperty("next_attempt")
	private final TimeValue nextAttempt;

	@JsonCreator
	public static Interval of(@JsonProperty("max_retry") int maxRetry, @JsonProperty("next_attempt") String nextAttempt) {
		return new Interval(maxRetry, TimeValue.parseTimeValue(nextAttempt));
	}
}