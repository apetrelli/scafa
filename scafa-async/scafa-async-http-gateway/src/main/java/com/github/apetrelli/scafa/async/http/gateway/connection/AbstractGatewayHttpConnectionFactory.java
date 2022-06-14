package com.github.apetrelli.scafa.async.http.gateway.connection;

import com.github.apetrelli.scafa.async.proto.processor.DataHandler;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractGatewayHttpConnectionFactory<T extends HttpAsyncSocket<HttpRequest>> implements GatewayHttpConnectionFactory<T> {

	protected final SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory;
	
	protected final ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory;
	
	protected final HostPort destinationSocketAddress;

	private final String interfaceName;

	private final boolean forceIpV4;

	@Override
	public T create(MappedGatewayHttpConnectionFactory<T> factory,
			AsyncSocket sourceChannel, HostPort socketAddress) {
		HttpAsyncSocket<HttpRequest> httpSocket = socketFactory.create(destinationSocketAddress, interfaceName, forceIpV4);
		return createConnection(factory, sourceChannel, httpSocket, socketAddress);
	}

	protected abstract T createConnection(MappedGatewayHttpConnectionFactory<T> factory, AsyncSocket sourceChannel,
			HttpAsyncSocket<HttpRequest> httpSocket, HostPort socketAddress);

}
