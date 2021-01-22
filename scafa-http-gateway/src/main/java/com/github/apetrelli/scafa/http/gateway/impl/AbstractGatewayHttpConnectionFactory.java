package com.github.apetrelli.scafa.http.gateway.impl;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.async.processor.DataHandler;
import com.github.apetrelli.scafa.proto.async.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public abstract class AbstractGatewayHttpConnectionFactory<T extends HttpAsyncSocket<HttpRequest>> implements GatewayHttpConnectionFactory<T> {

	protected SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory;
	
	protected ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory;
	
	protected HostPort destinationSocketAddress;

	private String interfaceName;

	private boolean forceIpV4;

	public AbstractGatewayHttpConnectionFactory(SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
			ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory, HostPort destinationSocketAddress,
			String interfaceName, boolean forceIpV4) {
		this.socketFactory = socketFactory;
		this.clientProcessorFactory = clientProcessorFactory;
		this.destinationSocketAddress = destinationSocketAddress;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
	}

	@Override
	public T create(MappedGatewayHttpConnectionFactory<T> factory,
			AsyncSocket sourceChannel, HostPort socketAddress) {
		HttpAsyncSocket<HttpRequest> httpSocket = socketFactory.create(destinationSocketAddress, interfaceName, forceIpV4);
		return createConnection(factory, sourceChannel, httpSocket, socketAddress);
	}

	protected abstract T createConnection(MappedGatewayHttpConnectionFactory<T> factory, AsyncSocket sourceChannel,
			HttpAsyncSocket<HttpRequest> httpSocket, HostPort socketAddress);

}
