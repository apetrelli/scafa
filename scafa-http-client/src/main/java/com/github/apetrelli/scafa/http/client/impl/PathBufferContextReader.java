package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.client.BufferContext;
import com.github.apetrelli.scafa.http.client.BufferContextReader;
import com.github.apetrelli.scafa.proto.util.IOUtils;

public class PathBufferContextReader implements BufferContextReader {

	private AsynchronousFileChannel channel;

	public PathBufferContextReader(AsynchronousFileChannel channel) {
		this.channel = channel;
	}

	@Override
	public void read(BufferContext context, CompletionHandler<Integer, BufferContext> completionHandler) {
		channel.read(context.getBuffer(), context.getPosition(), context,
				new CompletionHandler<Integer, BufferContext>() {

					@Override
					public void completed(Integer result, BufferContext attachment) {
						if (result >= 0) {
							context.moveForwardBy(result);
							context.getBuffer().flip();
						} else {
							IOUtils.closeQuietly(channel);
						}
						completionHandler.completed(result, attachment);
					}

					@Override
					public void failed(Throwable exc, BufferContext attachment) {
						IOUtils.closeQuietly(channel);
						completionHandler.failed(exc, attachment);
					}
				});
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}
}
