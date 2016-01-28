package io.mandrel.worker;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.frontier.Politeness;

import java.util.concurrent.TimeUnit;

import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * You shouldn't pass you fool!
 */
public class Barrier {

	private final Politeness politeness;
	private final DiscoveryClient discoveryClient;

	private TokenBucket pagePageRateBucket;
	private TokenBucket bandwidthBucket;
	private int nbWorkers = 0;

	public Barrier(Politeness politeness, DiscoveryClient discoveryClient) {
		this.politeness = politeness;
		this.discoveryClient = discoveryClient;

		updateBuckets();
	}

	public void updateBuckets() {
		int oldNbWorkers = nbWorkers;
		nbWorkers = discoveryClient.getInstances(ServiceIds.worker()).size();

		if (oldNbWorkers != nbWorkers) {
			if (politeness.getPageMeanRate() > 0) {
				pagePageRateBucket = TokenBuckets
						.builder()
						.withCapacity(
								politeness.getPagePeekRate() > 0 && politeness.getPagePeekRate() > politeness.getPageMeanRate() ? politeness.getPagePeekRate()
										/ nbWorkers : politeness.getPageMeanRate() / nbWorkers)
						.withFixedIntervalRefillStrategy(politeness.getPageMeanRate() / nbWorkers, 1, TimeUnit.SECONDS).build();
			} else {
				pagePageRateBucket = null;
			}

			if (politeness.getPageMeanRate() > 0) {
				bandwidthBucket = TokenBuckets
						.builder()
						.withCapacity(
								politeness.getMaxPeekBandwith() > 0 && politeness.getMaxPeekBandwith() > politeness.getMaxBandwith() ? politeness
										.getMaxPeekBandwith() / nbWorkers : politeness.getMaxBandwith() / nbWorkers)
						.withFixedIntervalRefillStrategy(politeness.getPageMeanRate() / nbWorkers, 1, TimeUnit.SECONDS).build();
			} else {
				bandwidthBucket = null;
			}
		}
	}

	public void passOrWait(Long consumedSize) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		pagePageRateBucket.consume();

		if (consumedSize != null) {
			bandwidthBucket.consume(consumedSize.longValue());
		}

		long elapsed = stopwatch.stop().elapsed(TimeUnit.SECONDS);

		if (politeness.getWait() > 0 && politeness.getWait() > elapsed) {
			Uninterruptibles.sleepUninterruptibly(politeness.getWait() - elapsed, TimeUnit.SECONDS);
		}
	}
}
