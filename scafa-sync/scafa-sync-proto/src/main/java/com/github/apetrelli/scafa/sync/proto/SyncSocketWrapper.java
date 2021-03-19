package com.github.apetrelli.scafa.sync.proto;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.proto.client.HostPort;

public class SyncSocketWrapper<T extends SyncSocket> implements SyncSocket {

	protected T socket;
	
	public SyncSocketWrapper(T socket) {
		this.socket = socket;
	}

	public HostPort getAddress() {
		return socket.getAddress();
	}

	@Override
	public void connect() {
		socket.connect();
	}
	
	@Override
	public void disconnect() {
		socket.disconnect();
	}
	
	@Override
	public int read(ByteBuffer buffer) {
		return socket.read(buffer);
	}
	
	@Override
	public int write(ByteBuffer buffer) {
		return socket.write(buffer);
	}

	public boolean isOpen() {
		return socket.isOpen();
	}
	
	@Override
	public void flushBuffer(ByteBuffer buffer) {
		socket.flushBuffer(buffer);
	}
	
	@Override
	public void flipAndFlushBuffer(ByteBuffer buffer) {
		socket.flipAndFlushBuffer(buffer);
	}
}
