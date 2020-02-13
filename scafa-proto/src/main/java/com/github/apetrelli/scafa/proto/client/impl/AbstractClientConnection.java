package com.github.apetrelli.scafa.proto.client.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;
import com.github.apetrelli.scafa.proto.client.ClientConnection;

public abstract class AbstractClientConnection<T extends AsyncSocket> implements ClientConnection {
    
    protected T socket;

    public AbstractClientConnection(T socket) {
    	this.socket = socket;
	}

	@Override
    public void ensureConnected(CompletionHandler<Void, Void> handler) {
		socket.connect(new DelegateFailureCompletionHandler<Void, Void>(handler) {

			@Override
			public void completed(Void result, Void attachment) {
                prepareChannel();
                handler.completed(result, attachment);
			}
		});
    }
	
	@Override
	public void disconnect(CompletionHandler<Void, Void> handler) {
		socket.disconnect(handler);
	}

	@Override
	public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
		socket.flushBuffer(buffer, completionHandler);
	}

	protected abstract void prepareChannel();

}
