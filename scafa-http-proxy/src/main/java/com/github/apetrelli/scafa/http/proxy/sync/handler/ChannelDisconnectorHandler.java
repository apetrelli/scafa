package com.github.apetrelli.scafa.http.proxy.sync.handler;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.proxy.sync.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;

public class ChannelDisconnectorHandler implements DataHandler {

	private MappedProxyHttpConnectionFactory factory;
	
	private SyncSocket socket;

	private HostPort socketAddress;

	public ChannelDisconnectorHandler(MappedProxyHttpConnectionFactory factory, SyncSocket socket, HostPort socketAddress) {
		this.factory = factory;
		this.socket = socket;
		this.socketAddress = socketAddress;
	}

	@Override
	public void onConnect() {
		// Do nothing
	}
	
	@Override
	public void onData(ByteBuffer buffer) {
		socket.flushBuffer(buffer);
	}

	@Override
	public void onDisconnect() {
		socket.disconnect(); // Ignore the outcome
		factory.dispose(socketAddress);
	}

}
