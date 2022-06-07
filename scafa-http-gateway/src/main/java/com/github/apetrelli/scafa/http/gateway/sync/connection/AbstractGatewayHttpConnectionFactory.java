package com.github.apetrelli.scafa.http.gateway.sync.connection;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.sync.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.sync.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

public abstract class AbstractGatewayHttpConnectionFactory<T extends HttpSyncSocket<HttpRequest>> implements GatewayHttpConnectionFactory<T> {

	protected SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory;
	
	protected ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory;
	
	protected RunnableStarter runnableStarter;
	
	protected HostPort destinationSocketAddress;

	private String interfaceName;

	private boolean forceIpV4;

	public AbstractGatewayHttpConnectionFactory(SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory, RunnableStarter runnableStarter,
			HostPort destinationSocketAddress, String interfaceName, boolean forceIpV4) {
		this.socketFactory = socketFactory;
		this.clientProcessorFactory = clientProcessorFactory;
		this.runnableStarter = runnableStarter;
		this.destinationSocketAddress = destinationSocketAddress;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
	}

	@Override
	public T create(MappedGatewayHttpConnectionFactory<T> factory,
			SyncSocket sourceChannel, HostPort socketAddress) {
		HttpSyncSocket<HttpRequest> httpSocket = socketFactory.create(destinationSocketAddress, interfaceName, forceIpV4);
		return createConnection(factory, sourceChannel, httpSocket, socketAddress);
	}

	protected abstract T createConnection(MappedGatewayHttpConnectionFactory<T> factory, SyncSocket sourceChannel,
			HttpSyncSocket<HttpRequest> httpSocket, HostPort socketAddress);

}
