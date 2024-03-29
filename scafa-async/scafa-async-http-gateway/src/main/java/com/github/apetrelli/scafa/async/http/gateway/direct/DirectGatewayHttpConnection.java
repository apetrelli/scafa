package com.github.apetrelli.scafa.async.http.gateway.direct;

import static com.github.apetrelli.scafa.http.HttpHeaders.HOST;

import com.github.apetrelli.scafa.async.proto.processor.DataHandler;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.gateway.connection.AbstractGatewayHttpConnection;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class DirectGatewayHttpConnection extends AbstractGatewayHttpConnection<AsyncSocket> {

	public DirectGatewayHttpConnection(MappedGatewayHttpConnectionFactory<?> factory,
			ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory, AsyncSocket sourceChannel,
			HttpAsyncSocket<HttpRequest> socket, HostPort destinationSocketAddress) {
		super(factory, clientProcessorFactory, sourceChannel, socket, destinationSocketAddress);
	}

	@Override
	protected HttpRequest createForwardedRequest(HttpRequest request) {
		HttpRequest realRequest = new HttpRequest(request);
		int port = destinationSocketAddress.getPort();
		realRequest.setHeader(HOST, new AsciiString(destinationSocketAddress.getHost() + (port == 80 ? "" : ":" + port)));
		return realRequest;
	}
}
