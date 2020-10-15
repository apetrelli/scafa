package com.github.apetrelli.scafa.http.gateway.direct;

import java.io.IOException;

import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.Handler;

public class ChannelDisconnectorHandler implements Handler {

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
	public void onDisconnect() {
		socket.disconnect();
		factory.dispose(socketAddress);
	}

}
