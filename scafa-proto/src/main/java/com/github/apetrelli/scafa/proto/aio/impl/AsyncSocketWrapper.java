package com.github.apetrelli.scafa.proto.aio.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class AsyncSocketWrapper<T extends AsyncSocket> implements AsyncSocket {

	protected T socket;
	
	public AsyncSocketWrapper(T socket) {
		this.socket = socket;
	}

	public HostPort getAddress() {
		return socket.getAddress();
	}

	public void connect(CompletionHandler<Void, Void> handler) {
		socket.connect(handler);
	}

	public void disconnect(CompletionHandler<Void, Void> handler) {
		socket.disconnect(handler);
	}

	public <A> void read(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler) {
		socket.read(buffer, attachment, handler);
	}

	public <A> void write(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler) {
		socket.write(buffer, attachment, handler);
	}

	public boolean isOpen() {
		return socket.isOpen();
	}
	
	@Override
	public void flushBuffer(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
		socket.flushBuffer(buffer, completionHandler);
	}
	
	@Override
	public void flipAndFlushBuffer(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
		socket.flipAndFlushBuffer(buffer, completionHandler);
	}
}
