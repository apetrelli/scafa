package com.github.apetrelli.scafa.http.gateway.impl;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public abstract class AbstractGatewayHttpConnectionFactory<T extends HttpAsyncSocket<HttpRequest>> implements GatewayHttpConnectionFactory<T> {

	protected SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory;
	
	protected ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory;
	
	protected HostPort destinationSocketAddress;

	public AbstractGatewayHttpConnectionFactory(SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
			ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory, HostPort destinationSocketAddress) {
		this.socketFactory = socketFactory;
		this.clientProcessorFactory = clientProcessorFactory;
		this.destinationSocketAddress = destinationSocketAddress;
	}

	@Override
	public T create(MappedGatewayHttpConnectionFactory<T> factory,
			AsyncSocket sourceChannel, HostPort socketAddress) {
		HttpAsyncSocket<HttpRequest> httpSocket = socketFactory.create(destinationSocketAddress, null, false);
		return createConnection(factory, sourceChannel, httpSocket);
	}

	protected abstract T createConnection(MappedGatewayHttpConnectionFactory<T> factory, AsyncSocket sourceChannel,
			HttpAsyncSocket<HttpRequest> httpSocket);

}
