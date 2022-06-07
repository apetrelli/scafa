package com.github.apetrelli.scafa.async.http.output.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.http.output.DataSender;
import com.github.apetrelli.scafa.async.proto.util.CompletionHandlerFuture;

public class NullDataSender implements DataSender {
	
	@Override
	public CompletableFuture<Void> send(ByteBuffer buffer) {
		// Discard all the data.
		buffer.position(buffer.limit());
		return CompletionHandlerFuture.completeEmpty();
	}
	
	@Override
	public CompletableFuture<Void> end() {
		return CompletionHandlerFuture.completeEmpty();
	}
}
