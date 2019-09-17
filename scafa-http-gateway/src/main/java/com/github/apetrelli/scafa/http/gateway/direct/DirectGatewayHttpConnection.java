package com.github.apetrelli.scafa.http.gateway.direct;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectGatewayHttpConnection extends AbstractDirectGatewayHttpConnection {

	public DirectGatewayHttpConnection(AsynchronousSocketChannel sourceChannel, HostPort socketAddress,
			String interfaceName, boolean forceIpV4, MappedGatewayHttpConnectionFactory factory) {
		super(sourceChannel, socketAddress, socketAddress, interfaceName, forceIpV4, factory);
	}

	@Override
	protected HttpRequest createForwardedRequest(HttpRequest request) throws IOException {
		HttpRequest realRequest = new HttpRequest(request);
		int port = destinationSocketAddress.getPort();
		realRequest.setHeader("Host", destinationSocketAddress.getHost() + (port == 80 ? "" : ":" + port));
		return realRequest;
	}
}
