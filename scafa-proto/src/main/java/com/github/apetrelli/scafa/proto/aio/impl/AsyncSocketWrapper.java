package com.github.apetrelli.scafa.proto.aio.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerResult;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class AsyncSocketWrapper<T extends AsyncSocket> implements AsyncSocket {

	protected T socket;
	
	public AsyncSocketWrapper(T socket) {
		this.socket = socket;
	}

	public HostPort getAddress() {
		return socket.getAddress();
	}

	@Override
	public CompletableFuture<Void> connect() {
		return socket.connect();
	}
	
	@Override
	public CompletableFuture<Void> disconnect() {
		return socket.disconnect();
	}
	
	@Override
	public <A> CompletableFuture<CompletionHandlerResult<Integer, A>> read(ByteBuffer buffer, A attachment) {
		return socket.read(buffer, attachment);
	}
	
	@Override
	public <A> CompletableFuture<CompletionHandlerResult<Integer, A>> write(ByteBuffer buffer, A attachment) {
		return socket.write(buffer, attachment);
	}

	public boolean isOpen() {
		return socket.isOpen();
	}
	
	@Override
	public CompletableFuture<Void> flushBuffer(ByteBuffer buffer) {
		return socket.flushBuffer(buffer);
	}
	
	@Override
	public CompletableFuture<Void> flipAndFlushBuffer(ByteBuffer buffer) {
		return flipAndFlushBuffer(buffer);
	}
}
