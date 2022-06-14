package com.github.apetrelli.scafa.async.proto.socket;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.client.HostPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AsyncSocketWrapper<T extends AsyncSocket> implements AsyncSocket {

	protected final T socket;

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
	public CompletableFuture<Integer> read(ByteBuffer buffer) {
		return socket.read(buffer);
	}
	
	@Override
	public CompletableFuture<Integer> write(ByteBuffer buffer) {
		return socket.write(buffer);
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
		return socket.flipAndFlushBuffer(buffer);
	}
}
