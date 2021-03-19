package com.github.apetrelli.scafa.sync.proto;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.proto.client.HostPort;

public interface SyncSocket {

	HostPort getAddress();
	
	void connect();
	
	void disconnect();
	
	int read(ByteBuffer buffer);
	
	int write(ByteBuffer buffer);
	
	default void flushBuffer(ByteBuffer buffer) {
		while (buffer.hasRemaining()) {
			write(buffer);
		}
	}
	
	default void flipAndFlushBuffer(ByteBuffer buffer) {
	    buffer.flip();
	    flushBuffer(buffer);
	    buffer.clear();
	}

	boolean isOpen();
}
