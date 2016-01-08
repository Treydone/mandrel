//package io.mandrel.common.thrift;
//
//import io.mandrel.common.net.Uri;
//import io.mandrel.common.sync.Container;
//import io.mandrel.endpoints.contractsv2.FrontierContract;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//
//import com.codahale.metrics.ConsoleReporter;
//import com.codahale.metrics.Meter;
//import com.codahale.metrics.MetricRegistry;
//import com.facebook.nifty.core.NiftyTimer;
//import com.facebook.nifty.duplex.TDuplexProtocolFactory;
//import com.facebook.nifty.processor.NiftyProcessor;
//import com.facebook.swift.codec.ThriftCodecManager;
//import com.facebook.swift.service.ThriftServer;
//import com.facebook.swift.service.ThriftServerConfig;
//import com.facebook.swift.service.ThriftServiceProcessor;
//import com.facebook.swift.service.ThriftServiceStatsHandler;
//import com.google.common.collect.ImmutableMap;
//import com.google.common.util.concurrent.ListenableFuture;
//
//public class SwiftServer {
//
//	public static final ImmutableMap<String, TDuplexProtocolFactory> DEFAULT_PROTOCOL_FACTORIES = ImmutableMap.<String, TDuplexProtocolFactory> builder()
//			.putAll(ThriftServer.DEFAULT_PROTOCOL_FACTORIES).put("zipped-binary", KeyedClientPool.protocolFactory(
////					Integer.valueOf(Deflater.BEST_SPEED)
//					null
//					))
//			.build();
//
//	public static void main(String[] args) throws InterruptedException, ExecutionException {
//
//		final MetricRegistry registry = new MetricRegistry();
//		final Meter requests = registry.meter(MetricRegistry.name(SwiftServer.class, "requests"));
//		final ConsoleReporter reporter = ConsoleReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
//				.build();
//		reporter.start(1, TimeUnit.SECONDS);
//
//		FrontierContract service = new FrontierContract() {
//
//			public List<Container> listContainers() {
//				requests.mark();
//				return Arrays.asList(null);
//			}
//
//			@Override
//			public void close() throws Exception {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public ListenableFuture<Uri> next(Long id) {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			@Override
//			public void delete(Long id, Uri uri) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void schedule(Long id, Uri uri) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void mschedule(Long id, Set<Uri> uris) {
//				// TODO Auto-generated method stub
//				
//			}
//		};
//
//		NiftyProcessor processor = new ThriftServiceProcessor(new ThriftCodecManager(), Arrays.asList(new ThriftServiceStatsHandler()), service);
//
//		ThriftServer server = new ThriftServer(processor, new ThriftServerConfig().setPort(9090).setProtocolName("zipped-binary"), new NiftyTimer("thrift"),
//				ThriftServer.DEFAULT_FRAME_CODEC_FACTORIES, DEFAULT_PROTOCOL_FACTORIES, ThriftServer.DEFAULT_WORKER_EXECUTORS,
//				ThriftServer.DEFAULT_SECURITY_FACTORY);
//		server.start();
//
//		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//			server.close();
//		}));
//	}
//}
