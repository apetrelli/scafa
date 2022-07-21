package com.github.apetrelli.scafa.sync.proto.jnet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocket;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectSyncServerSocket implements SyncServerSocket<com.github.apetrelli.scafa.proto.Socket> {

	private final ServerSocket channel;

	@Override
	public com.github.apetrelli.scafa.proto.Socket accept() {
		Socket socket;
		try {
			socket = channel.accept();
			DirectSyncSocket syncSocket = new DirectSyncSocket(socket);
			syncSocket.connect();
			return syncSocket;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}
	
	@Override
	public boolean isOpen() {
		return !channel.isClosed();
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}
}
