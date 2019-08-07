package com.github.apetrelli.scafa.http.proxy.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.processor.Handler;

public class ChannelDisconnectorHandler implements Handler {

	private MappedProxyHttpConnectionFactory factory;

	private AsynchronousSocketChannel channel;

	private HostPort socketAddress;

	public ChannelDisconnectorHandler(MappedProxyHttpConnectionFactory factory, AsynchronousSocketChannel channel,
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
			channel.close();
		}
	}

}
