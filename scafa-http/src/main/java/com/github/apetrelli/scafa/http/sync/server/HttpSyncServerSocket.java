package com.github.apetrelli.scafa.http.sync.server;

import java.io.IOException;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.http.sync.direct.DirectHttpSyncSocket;
import com.github.apetrelli.scafa.http.sync.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.sync.SyncServerSocket;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;

public class HttpSyncServerSocket<H extends HeaderHolder> implements SyncServerSocket<HttpSyncSocket<H>> {

	private final SyncServerSocket<SyncSocket> serverSocket;

	private final DataSenderFactory dataSenderFactory;

	public HttpSyncServerSocket(SyncServerSocket<SyncSocket> serverSocket, DataSenderFactory dataSenderFactory) {
		this.serverSocket = serverSocket;
		this.dataSenderFactory = dataSenderFactory;
	}

	@Override
	public HttpSyncSocket<H> accept() {
		SyncSocket socket = serverSocket.accept();
		return new DirectHttpSyncSocket<>(socket, dataSenderFactory);
	}
	
	@Override
	public boolean isOpen() {
		return serverSocket.isOpen();
	}

	@Override
	public void close() throws IOException {
		serverSocket.close();
	}

}
