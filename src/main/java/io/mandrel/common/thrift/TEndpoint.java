package io.mandrel.common.thrift;

import io.mandrel.common.data.Spider;
import io.mandrel.controller.ControllerContainers;
import io.mandrel.controller.thrift.Controller;
import io.mandrel.controller.thrift.Heartbeat;
import io.mandrel.frontier.FrontierContainer;
import io.mandrel.frontier.FrontierContainers;
import io.mandrel.frontier.thrift.Frontier;
import io.mandrel.worker.WorkerContainer;
import io.mandrel.worker.WorkerContainers;
import io.mandrel.worker.thrift.Worker;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.apache.thrift.TException;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.inject.Inject;
import com.netflix.servo.util.Throwables;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TEndpoint {

	private final ObjectMapper mapper;

	@PostConstruct
	public void run() {
		TMultiplexedProcessor processor = new TMultiplexedProcessor();
		processor.registerProcessor("Worker", new Worker.Processor<Worker.Iface>(new WorkerHandler(mapper)));
		processor.registerProcessor("Frontier", new Frontier.Processor<Frontier.Iface>(new FrontierHandler(mapper)));
		processor.registerProcessor("Controller", new Controller.Processor<Controller.Iface>(new ControllerHandler(mapper)));

		TNonblockingServerTransport transport;
		try {
			transport = new TNonblockingServerSocket(9090);
		} catch (TTransportException e) {
			throw Throwables.propagate(e);
		}

		TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(transport);
		args.transportFactory(new TFramedTransport.Factory());
		args.protocolFactory(new TBinaryProtocol.Factory());
		args.processor(processor);
		args.selectorThreads(4);
		args.workerThreads(32);
		TServer server = new TThreadedSelectorServer(args);

		server.serve();

		// Arrange to stop the server at shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				server.stop();
			}
		});
	}

	@RequiredArgsConstructor
	public static class ControllerHandler implements Controller.Iface {

		private final ObjectMapper mapper;

		@Override
		public void pulse(Heartbeat beat) throws TException {
			beat.getInfo();
		}

		@Override
		public void start(long id) throws TException {
			ControllerContainers.get(id).ifPresent(c -> c.start());
		}

		@Override
		public void pause(long id) throws TException {
			ControllerContainers.get(id).ifPresent(c -> c.pause());
		}

		@Override
		public void kill(long id) throws TException {
			ControllerContainers.get(id).ifPresent(c -> c.kill());
		}
	}

	@RequiredArgsConstructor
	public static class FrontierHandler implements Frontier.Iface {

		private final ObjectMapper mapper;

		@Override
		public void create(ByteBuffer definition) throws TException {
			Spider spider = null;
			try {
				spider = mapper.readValue(new ByteBufferBackedInputStream(definition), Spider.class);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// TODO
			FrontierContainer container = new FrontierContainer(spider, null);
			container.register();
		}

		@Override
		public void start(long id) throws TException {
			FrontierContainers.get(id).ifPresent(c -> c.start());
		}

		@Override
		public void pause(long id) throws TException {
			FrontierContainers.get(id).ifPresent(c -> c.pause());
		}

		@Override
		public void kill(long id) throws TException {
			FrontierContainers.get(id).ifPresent(c -> c.kill());
		}
	}

	@RequiredArgsConstructor
	public static class WorkerHandler implements Worker.Iface {

		private final ObjectMapper mapper;

		@Override
		public void create(ByteBuffer definition) throws TException {
			Spider spider = null;
			try {
				spider = mapper.readValue(new ByteBufferBackedInputStream(definition), Spider.class);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// TODO
			WorkerContainer container = new WorkerContainer(null, null, spider, null, null);
			container.register();
		}

		@Override
		public void start(long id) throws TException {
			WorkerContainers.get(id).ifPresent(c -> c.start());
		}

		@Override
		public void pause(long id) throws TException {
			WorkerContainers.get(id).ifPresent(c -> c.pause());
		}

		@Override
		public void kill(long id) throws TException {
			WorkerContainers.get(id).ifPresent(c -> c.kill());
		}
	}
}
