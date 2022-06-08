package com.github.apetrelli.scafa.sync.http.socket.server;

import java.io.IOException;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocket;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public class HttpSyncServerSocketFactory<H extends HeaderHolder> implements SyncServerSocketFactory<HttpSyncSocket<H>> {

	private final SyncServerSocketFactory<SyncSocket> factory;
	
	private final DataSenderFactory dataSenderFactory;
	
	public HttpSyncServerSocketFactory(SyncServerSocketFactory<SyncSocket> factory,
			DataSenderFactory dataSenderFactory) {
		this.factory = factory;
		this.dataSenderFactory = dataSenderFactory;
	}

	@Override
	public SyncServerSocket<HttpSyncSocket<H>> create() throws IOException {
		return new HttpSyncServerSocket<>(factory.create(), dataSenderFactory);
	}

}
