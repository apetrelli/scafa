package com.github.apetrelli.scafa.http.proxy.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.HandlerSupport;

public class ChannelDisconnectorHandler extends HandlerSupport implements DataHandler {

	private MappedProxyHttpConnectionFactory factory;
	
	private AsyncSocket socket;

	private HostPort socketAddress;

	public ChannelDisconnectorHandler(MappedProxyHttpConnectionFactory factory, AsyncSocket socket, HostPort socketAddress) {
		this.factory = factory;
		this.socket = socket;
		this.socketAddress = socketAddress;
	}
	
	@Override
	public CompletableFuture<Void> onData(ByteBuffer buffer) {
		return socket.flushBuffer(buffer);
	}

	@Override
	public void onDisconnect() {
		socket.disconnect(); // Ignore the outcome
		factory.dispose(socketAddress);
	}

}
