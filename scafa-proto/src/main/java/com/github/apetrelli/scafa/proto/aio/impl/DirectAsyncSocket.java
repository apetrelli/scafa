package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public class DirectAsyncSocket implements AsyncSocket {
	
	protected AsynchronousSocketChannel channel;
    
	public DirectAsyncSocket(AsynchronousSocketChannel channel) {
		this.channel = channel;
	}

	@Override
	public void disconnect(CompletionHandler<Void, Void> handler) {
        if (channel != null && channel.isOpen()) {
    		try {
    			channel.close();
    			handler.completed(null, null);
    		} catch (IOException e) {
    			handler.failed(e, null);
    		}
        } else {
			handler.completed(null, null);
        }
	}
	
	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public <A> void read(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler) {
		channel.read(buffer, attachment, handler);
	}

	@Override
	public <A> void write(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler) {
		channel.write(buffer, attachment, handler);
	}

}
