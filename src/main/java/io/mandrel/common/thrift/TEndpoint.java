package io.mandrel.common.thrift;

import io.mandrel.controller.thrift.Controller;
import io.mandrel.controller.thrift.Heartbeat;
import io.mandrel.frontier.thrift.Frontier;
import io.mandrel.worker.thrift.Worker;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;

import com.netflix.servo.util.Throwables;

public class TEndpoint {

	public void run() {
		TMultiplexedProcessor processor = new TMultiplexedProcessor();
		processor.registerProcessor("Worker", new Worker.Processor<Worker.Iface>(new WorkerHandler()));
		processor.registerProcessor("Frontier", new Frontier.Processor<Frontier.Iface>(new FrontierHandler()));
		processor.registerProcessor("Controller", new Controller.Processor<Controller.Iface>(new ControllerHandler()));

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

	public static class ControllerHandler implements Controller.Iface {

		@Override
		public void pulse(Heartbeat beat) throws TException {
			beat.getInfo();
		}

		@Override
		public void start(long id) throws TException {

		}

		@Override
		public void pause(long id) throws TException {

		}

		@Override
		public void kill(long id) throws TException {

		}
	}

	public static class FrontierHandler implements Frontier.Iface {

		@Override
		public void create(ByteBuffer definition) throws TException {

		}

		@Override
		public void start(long id) throws TException {

		}

		@Override
		public void pause(long id) throws TException {

		}

		@Override
		public void kill(long id) throws TException {

		}
	}

	public static class WorkerHandler implements Worker.Iface {

		@Override
		public void create(ByteBuffer definition) throws TException {

		}

		@Override
		public void start(long id) throws TException {

		}

		@Override
		public void pause(long id) throws TException {

		}

		@Override
		public void kill(long id) throws TException {

		}
	}
}
