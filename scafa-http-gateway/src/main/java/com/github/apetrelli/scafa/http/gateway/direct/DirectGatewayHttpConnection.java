package com.github.apetrelli.scafa.http.gateway.direct;

import java.io.IOException;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.ClientAsyncSocket;

public class DirectGatewayHttpConnection extends AbstractDirectGatewayHttpConnection {

	public DirectGatewayHttpConnection(AsyncSocket sourceChannel, ClientAsyncSocket socket,
			MappedGatewayHttpConnectionFactory factory) {
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
