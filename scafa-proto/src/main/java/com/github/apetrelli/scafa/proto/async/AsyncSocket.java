package com.github.apetrelli.scafa.proto.async;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.async.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.client.HostPort;

public interface AsyncSocket {

	HostPort getAddress();
	
	CompletableFuture<Void> connect();
	
	CompletableFuture<Void> disconnect();
	
	CompletableFuture<Integer> read(ByteBuffer buffer);
	
	CompletableFuture<Integer> write(ByteBuffer buffer);
	
	default CompletableFuture<Void> flushBuffer(ByteBuffer buffer) {
		return write(buffer).thenCompose(x -> {
			if (buffer.hasRemaining()) {
				return flushBuffer(buffer);
			}
			return CompletionHandlerFuture.completeEmpty();
		});
	}
	
	default CompletableFuture<Void> flipAndFlushBuffer(ByteBuffer buffer) {
	    buffer.flip();
	    return flushBuffer(buffer).thenRun(buffer::clear);
	}

	boolean isOpen();
}
