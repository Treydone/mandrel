package io.mandrel.metrics;

import io.mandrel.metrics.Timeserie.Data;

import java.time.LocalDateTime;
import java.util.TreeSet;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

public class Timeserie extends TreeSet<Data> {

	private static final long serialVersionUID = -3733297273926957662L;

	@lombok.Data
	@EqualsAndHashCode(exclude = "value")
	@AllArgsConstructor(staticName = "of")
	public static class Data implements Comparable<Data> {
		private final LocalDateTime time;
		private final Long value;

		@Override
		public int compareTo(Data other) {
			return other.getTime().compareTo(time);
		}
	}
}
