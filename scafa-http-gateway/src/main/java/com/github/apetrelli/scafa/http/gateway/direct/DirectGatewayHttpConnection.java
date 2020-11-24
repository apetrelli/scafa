package com.github.apetrelli.scafa.http.gateway.direct;

import java.io.IOException;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.impl.AbstractGatewayHttpConnection;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public class DirectGatewayHttpConnection extends AbstractGatewayHttpConnection<AsyncSocket> {

	public DirectGatewayHttpConnection(AsyncSocket sourceChannel, HttpAsyncSocket<HttpRequest> socket,
			MappedGatewayHttpConnectionFactory<?> factory) {
		super(sourceChannel, socket, factory);
	}

	@Override
	protected HttpRequest createForwardedRequest(HttpRequest request) throws IOException {
		HttpRequest realRequest = new HttpRequest(request);
		int port = destinationSocketAddress.getPort();
		realRequest.setHeader("Host", destinationSocketAddress.getHost() + (port == 80 ? "" : ":" + port));
		return realRequest;
	}
}
