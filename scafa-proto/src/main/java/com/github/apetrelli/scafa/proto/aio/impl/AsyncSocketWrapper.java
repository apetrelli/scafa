package com.github.apetrelli.scafa.proto.aio.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class AsyncSocketWrapper implements AsyncSocket {

	protected AsyncSocket socket;
	
	public AsyncSocketWrapper(AsyncSocket socket) {
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
}
