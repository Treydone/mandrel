package io.mandrel.common.thrift;

import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.facebook.nifty.test.LogEntry;
import com.facebook.nifty.test.ResultCode;
import com.facebook.nifty.test.Scribe;

@Slf4j
public class ThriftServerTest {

	public static void main(String[] args) throws TTransportException {

		final MetricRegistry registry = new MetricRegistry();
		final Meter requests = registry.meter(MetricRegistry.name(ThriftServerTest.class, "requests"));
		final ConsoleReporter reporter = ConsoleReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
		reporter.start(1, TimeUnit.SECONDS);

		// Create the handler
		Scribe.Iface serviceInterface = new Scribe.Iface() {

			@Override
			public ResultCode log(List<LogEntry> messages) throws TException {
				requests.mark();
				for (LogEntry message : messages) {
					log.info("{}: {}", message.getCategory(), message.getMessage());
				}
				return ResultCode.OK;
			}
		};

		TServerSocket serverTransport = new TServerSocket(7911);
		Scribe.Processor<Scribe.Iface> processor = new Scribe.Processor<Scribe.Iface>(serviceInterface);

		final TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

		server.serve();

		// Arrange to stop the server at shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				server.stop();
			}
		});
	}
}
