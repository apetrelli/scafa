package com.github.apetrelli.scafa.sync.http.gateway.handler;

import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.proto.processor.HandlerSupport;
import com.github.apetrelli.scafa.sync.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelDisconnectorHandler extends HandlerSupport implements DataHandler {

	private final MappedGatewayHttpConnectionFactory<?> factory;
	
	private final Socket socket;

	private final HostPort socketAddress;
	
	@Override
	public void onData(FlowBuffer buffer) {
		socket.out().write(buffer);
	}

	@Override
	public void onDisconnect() {
		socket.disconnect();
		factory.dispose(socketAddress);
	}

}
