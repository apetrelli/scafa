package com.github.apetrelli.scafa.sync.http.socket.server;

import java.io.IOException;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.http.socket.direct.DirectHttpSyncSocket;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocket;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

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
