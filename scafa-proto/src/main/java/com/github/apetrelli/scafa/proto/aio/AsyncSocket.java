package com.github.apetrelli.scafa.proto.aio;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.client.HostPort;

public interface AsyncSocket {

	HostPort getAddress();
	
	CompletableFuture<Void> connect();
	
	CompletableFuture<Void> disconnect();
	
	<A> CompletableFuture<CompletionHandlerResult<Integer, A>> read(ByteBuffer buffer, A attachment);
	
	<A> CompletableFuture<CompletionHandlerResult<Integer, A>> write(ByteBuffer buffer, A attachment);
	
	default CompletableFuture<Void> flushBuffer(ByteBuffer buffer) {
		return write(buffer, buffer).thenCompose(x -> {
			ByteBuffer attachment = x.getAttachment();
			if (attachment.hasRemaining()) {
				return flushBuffer(attachment);
			}
			return CompletionHandlerFuture.completeEmpty();
		});
	}
	
	default CompletableFuture<Void> flipAndFlushBuffer(ByteBuffer buffer) {
	    buffer.flip();
	    return flushBuffer(buffer);
	}

	boolean isOpen();
}
