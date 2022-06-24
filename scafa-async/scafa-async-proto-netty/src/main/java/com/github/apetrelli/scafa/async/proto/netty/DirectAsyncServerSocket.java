package com.github.apetrelli.scafa.async.proto.netty;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;

import io.netty.channel.socket.ServerSocketChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectAsyncServerSocket implements AsyncServerSocket<AsyncSocket>{
	
	private final SocketQueueManager socketQueueManager;
	
	private final ServerSocketChannel serverSocketChannel;
	
	@Override
	public CompletableFuture<AsyncSocket> accept() {
		return socketQueueManager.newCompletableFuture();
	}

	@Override
	public void close() throws IOException {
		try {
			serverSocketChannel.close().sync();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

}
