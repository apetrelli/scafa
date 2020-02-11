package com.github.apetrelli.scafa.http.gateway.direct;

import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnection;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsynchronousSocketChannelFactory;
import com.github.apetrelli.scafa.proto.aio.ClientAsyncSocket;
import com.github.apetrelli.scafa.proto.aio.impl.DirectClientAsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectGatewayHttpConnectionFactory implements GatewayHttpConnectionFactory {

	private AsynchronousSocketChannelFactory channelFactory;
	
	private HostPort destinationSocketAddress;

	public DirectGatewayHttpConnectionFactory(AsynchronousSocketChannelFactory channelFactory, HostPort destinationSocketAddress) {
		this.channelFactory = channelFactory;
		this.destinationSocketAddress = destinationSocketAddress;
	}

	@Override
	public GatewayHttpConnection create(MappedGatewayHttpConnectionFactory factory,
			AsyncSocket sourceChannel, HostPort socketAddress) {
		ClientAsyncSocket socket = new DirectClientAsyncSocket(channelFactory, destinationSocketAddress, null, false);
		return new DirectGatewayHttpConnection(sourceChannel, socket, factory);
	}

}
