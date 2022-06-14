package com.github.apetrelli.scafa.async.http.gateway.handler;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.proto.processor.DataHandler;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.HandlerSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelDisconnectorHandler extends HandlerSupport implements DataHandler {

	private final MappedGatewayHttpConnectionFactory<?> factory;
	
	private final AsyncSocket socket;

	private final HostPort socketAddress;
	
	@Override
	public CompletableFuture<Void> onData(ByteBuffer buffer) {
		return socket.flushBuffer(buffer);
	}

	@Override
	public void onDisconnect() {
		socket.disconnect();
		factory.dispose(socketAddress);
	}

}
