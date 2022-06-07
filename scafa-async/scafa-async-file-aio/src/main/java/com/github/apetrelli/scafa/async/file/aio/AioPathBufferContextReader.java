package com.github.apetrelli.scafa.async.file.aio;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.file.BufferContext;
import com.github.apetrelli.scafa.async.file.PathBufferContextReader;
import com.github.apetrelli.scafa.async.file.PathIOException;

public class AioPathBufferContextReader implements PathBufferContextReader {

	private AsynchronousFileChannel channel;

	public AioPathBufferContextReader(AsynchronousFileChannel channel) {
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
	public long size() {
		try {
			return channel.size();
		} catch (IOException e) {
			throw new PathIOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}
}
