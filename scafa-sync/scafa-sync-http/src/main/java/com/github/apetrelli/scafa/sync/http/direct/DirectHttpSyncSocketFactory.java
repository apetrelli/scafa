package com.github.apetrelli.scafa.sync.http.direct;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public class DirectHttpSyncSocketFactory implements SocketFactory<HttpSyncSocket<HttpRequest>> {

	private SocketFactory<SyncSocket> socketFactory;
	
	private DataSenderFactory dataSenderFactory;
	
	public DirectHttpSyncSocketFactory(SocketFactory<SyncSocket> socketFactory, DataSenderFactory dataSenderFactory) {
		this.socketFactory = socketFactory;
		this.dataSenderFactory = dataSenderFactory;
	}

	@Override
	public HttpSyncSocket<HttpRequest> create(HostPort hostPort, String interfaceName, boolean forceIpV4) {
		SyncSocket socket = socketFactory.create(hostPort, interfaceName, forceIpV4);
		return new DirectHttpSyncSocket<>(socket, dataSenderFactory);
	}

}
