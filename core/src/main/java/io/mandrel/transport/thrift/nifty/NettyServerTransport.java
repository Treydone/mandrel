package io.mandrel.transport.thrift.nifty;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.apache.thrift.protocol.TProtocolFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.jboss.netty.channel.local.LocalServerChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerBossPool;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.ThreadNameDeterminer;

import com.facebook.nifty.core.ChannelStatistics;
import com.facebook.nifty.core.ConnectionContextHandler;
import com.facebook.nifty.core.IdleDisconnectHandler;
import com.facebook.nifty.core.NettyServerConfig;
import com.facebook.nifty.core.NiftyDispatcher;
import com.facebook.nifty.core.NiftyExceptionLogger;
import com.facebook.nifty.core.NiftyMetrics;
import com.facebook.nifty.core.NiftySecurityHandlers;
import com.facebook.nifty.core.ShutdownUtil;
import com.facebook.nifty.core.ThriftServerDef;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@Slf4j
public class NettyServerTransport {

	private final int requestedPort;
	private int actualPort;
	private final ChannelPipelineFactory pipelineFactory;
	private static final int NO_WRITER_IDLE_TIMEOUT = 0;
	private static final int NO_ALL_IDLE_TIMEOUT = 0;
	private ServerBootstrap bootstrap;
	private final ChannelGroup allChannels;
	private ExecutorService bossExecutor;
	private ExecutorService ioWorkerExecutor;
	private ServerChannelFactory channelFactory;
	private Channel serverChannel;
	private final ThriftServerDef def;
	private final NettyServerConfig nettyServerConfig;
	private final ChannelStatistics channelStatistics;
	private final boolean local;

	@Inject
	public NettyServerTransport(final ThriftServerDef def, final NettyServerConfig nettyServerConfig, final ChannelGroup allChannels, final boolean local) {
		this.def = def;
		this.nettyServerConfig = nettyServerConfig;
		this.requestedPort = def.getServerPort();
		this.allChannels = allChannels;
		this.local = local;
		// connectionLimiter must be instantiated exactly once (and thus outside
		// the pipeline factory)
		final ConnectionLimiter connectionLimiter = new ConnectionLimiter(def.getMaxConnections());

		this.channelStatistics = new ChannelStatistics(allChannels);

		this.pipelineFactory = new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline cp = Channels.pipeline();
				TProtocolFactory inputProtocolFactory = def.getDuplexProtocolFactory().getInputProtocolFactory();
				NiftySecurityHandlers securityHandlers = def.getSecurityFactory().getSecurityHandlers(def, nettyServerConfig);
				cp.addLast("connectionContext", new ConnectionContextHandler());
				cp.addLast("connectionLimiter", connectionLimiter);
				cp.addLast(ChannelStatistics.NAME, channelStatistics);
				cp.addLast("encryptionHandler", securityHandlers.getEncryptionHandler());
				cp.addLast("frameCodec", def.getThriftFrameCodecFactory().create(def.getMaxFrameSize(), inputProtocolFactory));
				if (def.getClientIdleTimeout() != null) {
					// Add handlers to detect idle client connections and
					// disconnect them
					cp.addLast("idleTimeoutHandler", new IdleStateHandler(nettyServerConfig.getTimer(), def.getClientIdleTimeout().toMillis(),
							NO_WRITER_IDLE_TIMEOUT, NO_ALL_IDLE_TIMEOUT, TimeUnit.MILLISECONDS));
					cp.addLast("idleDisconnectHandler", new IdleDisconnectHandler());
				}

				cp.addLast("authHandler", securityHandlers.getAuthenticationHandler());
				cp.addLast("dispatcher", new NiftyDispatcher(def, nettyServerConfig.getTimer()));
				cp.addLast("exceptionLogger", new NiftyExceptionLogger());
				return cp;
			}
		};
	}

	public void start() {
		if (local) {
			channelFactory = new DefaultLocalServerChannelFactory();
		} else {
			bossExecutor = nettyServerConfig.getBossExecutor();
			int bossThreadCount = nettyServerConfig.getBossThreadCount();
			ioWorkerExecutor = nettyServerConfig.getWorkerExecutor();
			int ioWorkerThreadCount = nettyServerConfig.getWorkerThreadCount();
			channelFactory = new NioServerSocketChannelFactory(new NioServerBossPool(bossExecutor, bossThreadCount, ThreadNameDeterminer.CURRENT),
					new NioWorkerPool(ioWorkerExecutor, ioWorkerThreadCount, ThreadNameDeterminer.CURRENT));
		}

		start(channelFactory);
	}

	public void start(ServerChannelFactory serverChannelFactory) {
		bootstrap = new ServerBootstrap(serverChannelFactory);
		bootstrap.setOptions(nettyServerConfig.getBootstrapOptions());
		bootstrap.setPipelineFactory(pipelineFactory);
		serverChannel = bootstrap.bind(local ? new LocalAddress(requestedPort) : new InetSocketAddress(requestedPort));
	}

	public void stop() throws InterruptedException {
		if (serverChannel != null) {
			log.info("stopping transport %s:%s", def.getName(), actualPort);
			// first stop accepting
			final CountDownLatch latch = new CountDownLatch(1);
			serverChannel.close().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					// stop and process remaining in-flight invocations
					if (def.getExecutor() instanceof ExecutorService) {
						ExecutorService exe = (ExecutorService) def.getExecutor();
						ShutdownUtil.shutdownExecutor(exe, "dispatcher");
					}
					latch.countDown();
				}
			});
			latch.await();
			serverChannel = null;
		}

		// If the channelFactory was created by us, we should also clean it up.
		// If the
		// channelFactory was passed in by NiftyBootstrap, then it may be shared
		// so don't clean
		// it up.
		if (channelFactory != null) {
			ShutdownUtil.shutdownChannelFactory(channelFactory, bossExecutor, ioWorkerExecutor, allChannels);
		}
	}

	public Channel getServerChannel() {
		return serverChannel;
	}

	public int getPort() {
		if (actualPort != 0) {
			return actualPort;
		} else {
			return requestedPort; // may be 0 if server not yet started
		}
	}

	public void releaseExternalResources() {
		bootstrap.releaseExternalResources();
	}

	private static class ConnectionLimiter extends SimpleChannelUpstreamHandler {
		private final AtomicInteger numConnections;
		private final int maxConnections;

		public ConnectionLimiter(int maxConnections) {
			this.maxConnections = maxConnections;
			this.numConnections = new AtomicInteger(0);
		}

		@Override
		@SuppressWarnings("PMD.CollapsibleIfStatements")
		public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			if (maxConnections > 0) {
				if (numConnections.incrementAndGet() > maxConnections) {
					ctx.getChannel().close();
					// numConnections will be decremented in channelClosed
					log.info("Accepted connection above limit (%s). Dropping.", maxConnections);
				}
			}
			super.channelOpen(ctx, e);
		}

		@Override
		@SuppressWarnings("PMD.CollapsibleIfStatements")
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			if (maxConnections > 0) {
				if (numConnections.decrementAndGet() < 0) {
					log.error("BUG in ConnectionLimiter");
				}
			}
			super.channelClosed(ctx, e);
		}
	}

	public NiftyMetrics getMetrics() {
		return channelStatistics;
	}
}
