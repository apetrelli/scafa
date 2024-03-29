package com.github.apetrelli.scafa.sync.http.socket.direct;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectHttpSyncSocketFactory implements SocketFactory<HttpSyncSocket<HttpRequest>> {

	private final SocketFactory<SyncSocket> socketFactory;
	
	private final DataSenderFactory dataSenderFactory;

	@Override
	public HttpSyncSocket<HttpRequest> create(HostPort hostPort, String interfaceName, boolean forceIpV4) {
		SyncSocket socket = socketFactory.create(hostPort, interfaceName, forceIpV4);
		return new DirectHttpSyncSocket<>(socket, dataSenderFactory);
	}

}
