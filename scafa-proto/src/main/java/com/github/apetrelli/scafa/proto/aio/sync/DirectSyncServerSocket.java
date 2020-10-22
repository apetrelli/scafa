package com.github.apetrelli.scafa.proto.aio.sync;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.aio.AsyncServerSocket;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public class DirectSyncServerSocket implements AsyncServerSocket<AsyncSocket> {

	private ServerSocket channel;

	public DirectSyncServerSocket(ServerSocket channel) {
		this.channel = channel;
	}

	@Override
	public CompletableFuture<AsyncSocket> accept() {
		Socket socket;
		try {
			socket = channel.accept();
			return CompletableFuture.completedFuture(new DirectSyncSocket(socket));
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}
}
