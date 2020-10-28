package com.github.apetrelli.scafa.proto.sync.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.github.apetrelli.scafa.proto.sync.IORuntimeException;
import com.github.apetrelli.scafa.proto.sync.SyncServerSocket;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;

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
			return new DirectSyncSocket(socket);
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
