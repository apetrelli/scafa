package com.github.apetrelli.scafa.http.gateway.direct;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.impl.DirectHttpAsyncSocket;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsyncSocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectGatewayHttpConnectionFactory implements GatewayHttpConnectionFactory {

	private AsyncSocketFactory<AsyncSocket> socketFactory;
	
	private DataSenderFactory dataSenderFactory;
	
	private HostPort destinationSocketAddress;

	public DirectGatewayHttpConnectionFactory(AsyncSocketFactory<AsyncSocket> socketFactory,
			DataSenderFactory dataSenderFactory, HostPort destinationSocketAddress) {
		this.socketFactory = socketFactory;
		this.dataSenderFactory = dataSenderFactory;
		this.destinationSocketAddress = destinationSocketAddress;
	}

	@Override
	public HttpAsyncSocket<HttpRequest> create(MappedGatewayHttpConnectionFactory factory,
			AsyncSocket sourceChannel, HostPort socketAddress) {
		AsyncSocket socket = socketFactory.create(destinationSocketAddress, null, false);
		HttpAsyncSocket<HttpRequest> httpSocket = new DirectHttpAsyncSocket<>(socket, dataSenderFactory);
		return new DirectGatewayHttpConnection(sourceChannel, httpSocket, factory);
	}

}
