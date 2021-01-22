package com.github.apetrelli.scafa.http.async.direct;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.async.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.async.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectHttpAsyncSocketFactory implements SocketFactory<HttpAsyncSocket<HttpRequest>> {

	private SocketFactory<AsyncSocket> socketFactory;
	
	private DataSenderFactory dataSenderFactory;
	
	public DirectHttpAsyncSocketFactory(SocketFactory<AsyncSocket> socketFactory, DataSenderFactory dataSenderFactory) {
		this.socketFactory = socketFactory;
		this.dataSenderFactory = dataSenderFactory;
	}

	@Override
	public HttpAsyncSocket<HttpRequest> create(HostPort hostPort, String interfaceName, boolean forceIpV4) {
		AsyncSocket socket = socketFactory.create(hostPort, interfaceName, forceIpV4);
		return new DirectHttpAsyncSocket<>(socket, dataSenderFactory);
	}

}
