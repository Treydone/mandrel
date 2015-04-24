package io.mandrel.service.stats;

import java.util.LongSummaryStatistics;

import lombok.Data;

@Data
public class Summary {

	private LongSummaryStatistics pageSize;
}
