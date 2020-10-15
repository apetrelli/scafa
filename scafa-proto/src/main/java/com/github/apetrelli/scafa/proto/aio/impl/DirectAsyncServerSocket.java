package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.aio.AsyncServerSocket;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsyncSocketFactory;

public class DirectAsyncServerSocket<T extends AsyncSocket> implements AsyncServerSocket<T> {

	private AsynchronousServerSocketChannel channel;

	private AsyncSocketFactory<T> asyncSocketFactory;

	public DirectAsyncServerSocket(AsynchronousServerSocketChannel channel, AsyncSocketFactory<T> asyncSocketFactory) {
		this.channel = channel;
		this.asyncSocketFactory = asyncSocketFactory;
	}

	@Override
	public CompletableFuture<T> accept() {
		CompletableFuture<T> future = new CompletableFuture<>();
		channel.accept(future, new CompletionHandler<>() {

			@Override
			public void completed(AsynchronousSocketChannel result, CompletableFuture<T> attachment) {
				attachment.complete(asyncSocketFactory.create(result));
			}

			@Override
			public void failed(Throwable exc, CompletableFuture<T> attachment) {
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
