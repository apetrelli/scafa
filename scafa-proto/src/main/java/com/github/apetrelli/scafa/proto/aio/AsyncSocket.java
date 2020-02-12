package com.github.apetrelli.scafa.proto.aio;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.client.HostPort;

public interface AsyncSocket {

	HostPort getAddress();
	
	void connect(CompletionHandler<Void, Void> handler);
	
	void disconnect(CompletionHandler<Void, Void> handler);
	
	<A> void read(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler);
	
	<A> void write(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler);

	boolean isOpen();
}
