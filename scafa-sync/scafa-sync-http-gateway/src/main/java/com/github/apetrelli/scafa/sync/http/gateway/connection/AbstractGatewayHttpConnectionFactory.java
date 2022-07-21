package com.github.apetrelli.scafa.sync.http.gateway.connection;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractGatewayHttpConnectionFactory<T extends HttpSyncSocket<HttpRequest>> implements GatewayHttpConnectionFactory<T> {

	protected final SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory;
	
	protected final ProcessorFactory<DataHandler, Socket> clientProcessorFactory;
	
	protected final RunnableStarter runnableStarter;
	
	protected final HostPort destinationSocketAddress;

	private final String interfaceName;

	private final boolean forceIpV4;

	@Override
	public T create(MappedGatewayHttpConnectionFactory<T> factory,
			Socket sourceChannel, HostPort socketAddress) {
		HttpSyncSocket<HttpRequest> httpSocket = socketFactory.create(destinationSocketAddress, interfaceName, forceIpV4);
		return createConnection(factory, sourceChannel, httpSocket, socketAddress);
	}

	protected abstract T createConnection(MappedGatewayHttpConnectionFactory<T> factory, Socket sourceChannel,
			HttpSyncSocket<HttpRequest> httpSocket, HostPort socketAddress);

}
