package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.aio.BufferContext;
import com.github.apetrelli.scafa.proto.aio.BufferContextReader;
import com.github.apetrelli.scafa.tls.util.IOUtils;

public class PathBufferContextReader implements BufferContextReader {

	private AsynchronousFileChannel channel;

	public PathBufferContextReader(AsynchronousFileChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public CompletableFuture<Integer> read(BufferContext context) {
		CompletableFuture<Integer> future = new CompletableFuture<>();
		channel.read(context.getBuffer(), context.getPosition(), future, new CompletionHandler<Integer, CompletableFuture<Integer>>() {

			@Override
			public void completed(Integer result, CompletableFuture<Integer> attachment) {
				if (result >= 0) {
					context.moveForwardBy(result);
					context.getBuffer().flip();
				} else {
					IOUtils.closeQuietly(channel);
				}
				attachment.complete(result);
			}
			
			@Override
			public void failed(Throwable exc, CompletableFuture<Integer> attachment) {
				IOUtils.closeQuietly(channel);
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
