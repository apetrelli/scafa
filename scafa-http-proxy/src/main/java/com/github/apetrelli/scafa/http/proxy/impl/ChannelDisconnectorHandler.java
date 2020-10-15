package com.github.apetrelli.scafa.http.proxy.impl;

import java.io.IOException;

import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.Handler;

public class ChannelDisconnectorHandler implements Handler {

	private MappedProxyHttpConnectionFactory factory;
	
	private AsyncSocket socket;

	private HostPort socketAddress;

	public ChannelDisconnectorHandler(MappedProxyHttpConnectionFactory factory, AsyncSocket socket, HostPort socketAddress) {
		this.factory = factory;
		this.socket = socket;
		this.socketAddress = socketAddress;
	}

	@Override
	public void onConnect() throws IOException {
		// Do nothing
	}

	@Override
	public void onDisconnect() {
		socket.disconnect(); // Ignore the outcome
		factory.dispose(socketAddress);
	}

}
