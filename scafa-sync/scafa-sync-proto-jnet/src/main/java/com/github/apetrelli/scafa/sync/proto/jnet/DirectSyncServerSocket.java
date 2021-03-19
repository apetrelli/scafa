package com.github.apetrelli.scafa.sync.proto.jnet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocket;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public class DirectSyncServerSocket implements SyncServerSocket<SyncSocket> {

	private ServerSocket channel;

	public DirectSyncServerSocket(ServerSocket channel) {
		this.channel = channel;
	}

	@Override
	public SyncSocket accept() {
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
