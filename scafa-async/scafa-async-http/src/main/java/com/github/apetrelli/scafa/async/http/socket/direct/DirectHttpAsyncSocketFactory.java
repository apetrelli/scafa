package com.github.apetrelli.scafa.async.http.socket.direct;

import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectHttpAsyncSocketFactory implements SocketFactory<HttpAsyncSocket<HttpRequest>> {

	private final SocketFactory<AsyncSocket> socketFactory;
	
	private final DataSenderFactory dataSenderFactory;

	@Override
	public HttpAsyncSocket<HttpRequest> create(HostPort hostPort, String interfaceName, boolean forceIpV4) {
		AsyncSocket socket = socketFactory.create(hostPort, interfaceName, forceIpV4);
		return new DirectHttpAsyncSocket<>(socket, dataSenderFactory);
	}

}
