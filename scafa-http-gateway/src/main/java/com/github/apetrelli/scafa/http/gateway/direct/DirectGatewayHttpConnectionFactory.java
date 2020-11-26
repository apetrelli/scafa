package com.github.apetrelli.scafa.http.gateway.direct;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.impl.AbstractGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class DirectGatewayHttpConnectionFactory extends AbstractGatewayHttpConnectionFactory<HttpAsyncSocket<HttpRequest>> {

	public DirectGatewayHttpConnectionFactory(SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
			ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory, HostPort destinationSocketAddress) {
		super(socketFactory, clientProcessorFactory, destinationSocketAddress);
	}

	@Override
	protected HttpAsyncSocket<HttpRequest> createConnection(
			MappedGatewayHttpConnectionFactory<HttpAsyncSocket<HttpRequest>> factory, AsyncSocket sourceChannel,
			HttpAsyncSocket<HttpRequest> httpSocket) {
		return new DirectGatewayHttpConnection(factory, clientProcessorFactory, sourceChannel, httpSocket, destinationSocketAddress);
	}
}
