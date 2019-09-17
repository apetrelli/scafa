package com.github.apetrelli.scafa.http.gateway.direct;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnection;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectGatewayHttpConnectionFactory implements GatewayHttpConnectionFactory {

	private HostPort destinationSocketAddress;

	public DirectGatewayHttpConnectionFactory(HostPort destinationSocketAddress) {
		this.destinationSocketAddress = destinationSocketAddress;
	}

	@Override
	public GatewayHttpConnection create(MappedGatewayHttpConnectionFactory factory,
			AsynchronousSocketChannel sourceChannel, HostPort socketAddress) {
		return new DirectGatewayHttpConnection(sourceChannel, destinationSocketAddress, null, false, factory);
	}

}
