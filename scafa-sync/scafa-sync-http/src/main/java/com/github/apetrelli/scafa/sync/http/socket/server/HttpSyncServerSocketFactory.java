package com.github.apetrelli.scafa.sync.http.socket.server;

import java.io.IOException;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocket;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpSyncServerSocketFactory<H extends HeaderHolder> implements SyncServerSocketFactory<HttpSyncSocket<H>> {

	private final SyncServerSocketFactory<Socket> factory;
	
	private final DataSenderFactory dataSenderFactory;

	@Override
	public SyncServerSocket<HttpSyncSocket<H>> create() throws IOException {
		return new HttpSyncServerSocket<>(factory.create(), dataSenderFactory);
	}

}
