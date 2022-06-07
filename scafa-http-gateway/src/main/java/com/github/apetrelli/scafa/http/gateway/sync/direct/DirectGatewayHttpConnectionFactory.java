package com.github.apetrelli.scafa.http.gateway.sync.direct;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.sync.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.sync.connection.AbstractGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

public class DirectGatewayHttpConnectionFactory extends AbstractGatewayHttpConnectionFactory<HttpSyncSocket<HttpRequest>> {

	public DirectGatewayHttpConnectionFactory(SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory, RunnableStarter runnableStarter, HostPort destinationSocketAddress) {
		super(socketFactory, clientProcessorFactory, runnableStarter, destinationSocketAddress, null, false);
	}

	@Override
	protected HttpSyncSocket<HttpRequest> createConnection(
			MappedGatewayHttpConnectionFactory<HttpSyncSocket<HttpRequest>> factory, SyncSocket sourceChannel,
			HttpSyncSocket<HttpRequest> httpSocket, HostPort socketAddress) {
		// The socket address is in the original request, so it must be ignored for a gateway.
		return new DirectGatewayHttpConnection(factory, clientProcessorFactory, runnableStarter, sourceChannel,
				httpSocket, destinationSocketAddress);
	}
}
