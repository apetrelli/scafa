package com.github.apetrelli.scafa.http.gateway.direct;

import com.github.apetrelli.scafa.async.proto.processor.DataHandler;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.impl.AbstractGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class DirectGatewayHttpConnectionFactory extends AbstractGatewayHttpConnectionFactory<HttpAsyncSocket<HttpRequest>> {

	public DirectGatewayHttpConnectionFactory(SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
			ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory, HostPort destinationSocketAddress) {
		super(socketFactory, clientProcessorFactory, destinationSocketAddress, null, false);
	}

	@Override
	protected HttpAsyncSocket<HttpRequest> createConnection(
			MappedGatewayHttpConnectionFactory<HttpAsyncSocket<HttpRequest>> factory, AsyncSocket sourceChannel,
			HttpAsyncSocket<HttpRequest> httpSocket, HostPort socketAddress) {
		// The socket address is in the original request, so it must be ignored for a gateway.
		return new DirectGatewayHttpConnection(factory, clientProcessorFactory, sourceChannel, httpSocket, destinationSocketAddress);
	}
}
