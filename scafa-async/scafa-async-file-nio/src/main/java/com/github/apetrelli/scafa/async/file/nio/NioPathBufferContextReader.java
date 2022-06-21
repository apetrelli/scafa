package com.github.apetrelli.scafa.async.file.nio;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.file.BufferContext;
import com.github.apetrelli.scafa.async.file.PathBufferContextReader;
import com.github.apetrelli.scafa.async.file.PathIOException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NioPathBufferContextReader implements PathBufferContextReader {

	private final FileChannel channel;
	
	@Override
	public CompletableFuture<Integer> read(BufferContext context) {
		CompletableFuture<Integer> future = new CompletableFuture<>();
		int result;
		try {
			result = channel.read(context.getBuffer(), context.getPosition());
			if (result >= 0) {
				context.moveForwardBy(result);
				context.getBuffer().flip();
			} else {
				IOUtils.closeQuietly(channel);
			}
			future.complete(result);
		} catch (IOException e) {
			IOUtils.closeQuietly(channel);
			future.completeExceptionally(e);
		}

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
