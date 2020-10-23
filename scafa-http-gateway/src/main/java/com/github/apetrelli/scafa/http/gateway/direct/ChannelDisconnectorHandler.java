package com.github.apetrelli.scafa.http.gateway.direct;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.DataHandler;

public class ChannelDisconnectorHandler implements DataHandler {

	private MappedGatewayHttpConnectionFactory factory;
	
	private AsyncSocket socket;

	private HostPort socketAddress;

	public ChannelDisconnectorHandler(MappedGatewayHttpConnectionFactory factory,
			AsyncSocket socket, HostPort socketAddress) {
		this.factory = factory;
		this.socket = socket;
		this.socketAddress = socketAddress;
	}

	@Override
	public void onConnect() throws IOException {
		// Do nothing
	}
	
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
