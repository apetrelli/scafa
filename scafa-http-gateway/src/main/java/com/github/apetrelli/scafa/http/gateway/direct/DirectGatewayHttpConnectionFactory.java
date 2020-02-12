package com.github.apetrelli.scafa.http.gateway.direct;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnection;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.impl.DirectHttpAsyncSocket;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsynchronousSocketChannelFactory;
import com.github.apetrelli.scafa.proto.aio.impl.DirectClientAsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectGatewayHttpConnectionFactory implements GatewayHttpConnectionFactory {

	private AsynchronousSocketChannelFactory channelFactory;
	
	private DataSenderFactory dataSenderFactory;
	
	private HostPort destinationSocketAddress;

	public DirectGatewayHttpConnectionFactory(AsynchronousSocketChannelFactory channelFactory,
			DataSenderFactory dataSenderFactory, HostPort destinationSocketAddress) {
		this.channelFactory = channelFactory;
		this.dataSenderFactory = dataSenderFactory;
		this.destinationSocketAddress = destinationSocketAddress;
	}

	@Override
	public GatewayHttpConnection create(MappedGatewayHttpConnectionFactory factory,
			AsyncSocket sourceChannel, HostPort socketAddress) {
		AsyncSocket socket = new DirectClientAsyncSocket(channelFactory, destinationSocketAddress, null, false);
		HttpAsyncSocket httpSocket = new DirectHttpAsyncSocket(socket, dataSenderFactory);
		return new DirectGatewayHttpConnection(sourceChannel, httpSocket, factory);
	}

}
