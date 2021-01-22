package com.github.apetrelli.scafa.http.sync.output.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.async.output.DataSender;
import com.github.apetrelli.scafa.proto.async.util.CompletionHandlerFuture;

public class NullDataSender implements DataSender {
	
	@Override
	public CompletableFuture<Void> send(ByteBuffer buffer) {
		return CompletionHandlerFuture.completeEmpty();
	}
	
	@Override
	public CompletableFuture<Void> end() {
		return CompletionHandlerFuture.completeEmpty();
	}
}
