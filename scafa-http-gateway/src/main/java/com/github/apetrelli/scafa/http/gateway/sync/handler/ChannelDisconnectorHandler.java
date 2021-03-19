package com.github.apetrelli.scafa.http.gateway.sync.handler;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.gateway.sync.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.HandlerSupport;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;


public class ChannelDisconnectorHandler extends HandlerSupport implements DataHandler {

	private MappedGatewayHttpConnectionFactory<?> factory;
	
	private SyncSocket socket;

	private HostPort socketAddress;

	public ChannelDisconnectorHandler(MappedGatewayHttpConnectionFactory<?> factory,
			SyncSocket socket, HostPort socketAddress) {
		this.factory = factory;
		this.socket = socket;
		this.socketAddress = socketAddress;
	}
	
	@Override
	public void onData(ByteBuffer buffer) {
		socket.flushBuffer(buffer);
	}

	@Override
	public void onDisconnect() {
		socket.disconnect();
		factory.dispose(socketAddress);
	}

}
