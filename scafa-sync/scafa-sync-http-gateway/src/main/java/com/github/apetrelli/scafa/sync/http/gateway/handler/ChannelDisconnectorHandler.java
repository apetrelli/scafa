package com.github.apetrelli.scafa.sync.http.gateway.handler;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.HandlerSupport;
import com.github.apetrelli.scafa.sync.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelDisconnectorHandler extends HandlerSupport implements DataHandler {

	private final MappedGatewayHttpConnectionFactory<?> factory;
	
	private final SyncSocket socket;

	private final HostPort socketAddress;
	
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
