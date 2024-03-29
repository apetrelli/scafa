package com.github.apetrelli.scafa.sync.http.gateway.direct;

import static com.github.apetrelli.scafa.http.HttpHeaders.HOST;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.gateway.connection.AbstractGatewayHttpConnection;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.util.AsciiString;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

public class DirectGatewayHttpConnection extends AbstractGatewayHttpConnection<SyncSocket> {

	public DirectGatewayHttpConnection(MappedGatewayHttpConnectionFactory<?> factory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory, RunnableStarter runnableStarter,
			SyncSocket sourceChannel, HttpSyncSocket<HttpRequest> socket, HostPort destinationSocketAddress) {
		super(factory, clientProcessorFactory, runnableStarter, sourceChannel, socket, destinationSocketAddress);
	}

	@Override
	protected HttpRequest createForwardedRequest(HttpRequest request) {
		HttpRequest realRequest = new HttpRequest(request);
		int port = destinationSocketAddress.getPort();
		realRequest.setHeader(HOST, new AsciiString(destinationSocketAddress.getHost() + (port == 80 ? "" : ":" + port)));
		return realRequest;
	}
}
