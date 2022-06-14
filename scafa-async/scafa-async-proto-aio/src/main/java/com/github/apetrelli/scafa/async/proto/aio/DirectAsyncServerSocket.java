package com.github.apetrelli.scafa.async.proto.aio;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectAsyncServerSocket implements AsyncServerSocket<AsyncSocket> {

	private final AsynchronousServerSocketChannel channel;

	@Override
	public CompletableFuture<AsyncSocket> accept() {
		CompletableFuture<AsyncSocket> future = new CompletableFuture<>();
		channel.accept(future, new CompletionHandler<>() {

			@Override
			public void completed(AsynchronousSocketChannel result, CompletableFuture<AsyncSocket> attachment) {
				attachment.complete(new DirectAsyncSocket(result));
			}

			@Override
			public void failed(Throwable exc, CompletableFuture<AsyncSocket> attachment) {
				attachment.completeExceptionally(exc);
			}
		});
		return future;
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}
}
