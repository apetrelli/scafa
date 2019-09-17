package com.github.apetrelli.scafa.http.gateway.direct;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.util.IOUtils;

public class ChannelDisconnectorHandler implements Handler {

	private MappedGatewayHttpConnectionFactory factory;

	private AsynchronousSocketChannel channel;

	private HostPort socketAddress;

	public ChannelDisconnectorHandler(MappedGatewayHttpConnectionFactory factory, AsynchronousSocketChannel channel,
            HostPort socketAddress) {
		this.factory = factory;
		this.channel = channel;
		this.socketAddress = socketAddress;
	}

	@Override
	public void onConnect() throws IOException {
		// Do nothing
	}

	@Override
	public void onDisconnect() throws IOException {
		factory.dispose(socketAddress);
		if (channel.isOpen()) {
			IOUtils.closeQuietly(channel);
		}
	}

}
