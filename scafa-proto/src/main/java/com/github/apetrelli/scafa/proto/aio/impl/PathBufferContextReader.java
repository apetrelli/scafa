package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.aio.BufferContext;
import com.github.apetrelli.scafa.proto.aio.BufferContextReader;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerResult;
import com.github.apetrelli.scafa.tls.util.IOUtils;

public class PathBufferContextReader implements BufferContextReader {

	private AsynchronousFileChannel channel;

	public PathBufferContextReader(AsynchronousFileChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public CompletableFuture<CompletionHandlerResult<Integer, BufferContext>> read(BufferContext context) {
		CompletableFuture<CompletionHandlerResult<Integer, BufferContext>> future = new CompletableFuture<>();
		channel.read(context.getBuffer(), context.getPosition(), new CompletableFutureAttachmentPair<>(future, context), new CompletionHandler<Integer, CompletableFutureAttachmentPair<Integer, BufferContext>>() {

			@Override
			public void completed(Integer result, CompletableFutureAttachmentPair<Integer, BufferContext> attachment) {
				BufferContext context = attachment.getAttachment();
				if (result >= 0) {
					context.moveForwardBy(result);
					context.getBuffer().flip();
				} else {
					IOUtils.closeQuietly(channel);
				}
				attachment.getFuture().complete(new CompletionHandlerResult<>(result, context));
			}
			
			@Override
			public void failed(Throwable exc, CompletableFutureAttachmentPair<Integer, BufferContext> attachment) {
				IOUtils.closeQuietly(channel);
				attachment.getFuture().completeExceptionally(new CompletionHandlerException(attachment.getAttachment(), exc));
			}
		});

		return future;
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}
}
