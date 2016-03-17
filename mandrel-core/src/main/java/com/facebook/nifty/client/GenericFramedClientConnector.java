package com.facebook.nifty.client;

import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

import com.facebook.nifty.duplex.TDuplexProtocolFactory;
import com.google.common.net.HostAndPort;

/***
 * A {@link NiftyClientConnector} specialized for {@link GenericFramedClientConnector}
 */
public class GenericFramedClientConnector extends AbstractClientConnector<FramedClientChannel> {
	// TFramedTransport framing appears at the front of the message
	private static final int LENGTH_FIELD_OFFSET = 0;

	// TFramedTransport framing is four bytes long
	private static final int LENGTH_FIELD_LENGTH = 4;

	// TFramedTransport framing represents message size *not including* framing so no adjustment
	// is necessary
	private static final int LENGTH_ADJUSTMENT = 0;

	// The client expects to see only the message *without* any framing, this strips it off
	private static final int INITIAL_BYTES_TO_STRIP = LENGTH_FIELD_LENGTH;

	public GenericFramedClientConnector(SocketAddress address) {
		this(address, defaultProtocolFactory());
	}

	public GenericFramedClientConnector(HostAndPort address) {
		this(address, defaultProtocolFactory());
	}

	public GenericFramedClientConnector(SocketAddress address, TDuplexProtocolFactory protocolFactory) {
		super(address, protocolFactory);
	}

	public GenericFramedClientConnector(HostAndPort address, TDuplexProtocolFactory protocolFactory) {
		super(toSocketAddress(address), protocolFactory);
	}

	@Override
	public FramedClientChannel newThriftClientChannel(Channel nettyChannel, NettyClientConfig clientConfig) {
		GenericFramedClientChannel channel = new GenericFramedClientChannel(nettyChannel, clientConfig.getTimer(), getProtocolFactory());
		ChannelPipeline cp = nettyChannel.getPipeline();
		TimeoutHandler.addToPipeline(cp);
		cp.addLast("thriftHandler", channel);
		return channel;
	}

	@Override
	public ChannelPipelineFactory newChannelPipelineFactory(final int maxFrameSize, NettyClientConfig clientConfig) {
		return new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline cp = Channels.pipeline();
				TimeoutHandler.addToPipeline(cp);
				cp.addLast("frameEncoder", new LengthFieldPrepender(LENGTH_FIELD_LENGTH));
				cp.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(maxFrameSize, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT,
						INITIAL_BYTES_TO_STRIP));
				return cp;
			}
		};
	}
}
