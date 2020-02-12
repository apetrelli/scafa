package com.github.apetrelli.scafa.proto.client.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;
import com.github.apetrelli.scafa.proto.aio.util.AsyncUtils;
import com.github.apetrelli.scafa.proto.client.ClientConnection;
import com.github.apetrelli.scafa.tls.util.CompletionHandlerFuture;

public abstract class AbstractClientConnection implements ClientConnection {
    
    protected AsyncSocket socket;

    public AbstractClientConnection(AsyncSocket socket) {
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
		AsyncUtils.flushBuffer(buffer, socket, completionHandler);
	}

    @Override
    public void close() throws IOException {
    	CompletionHandlerFuture<Void, Void> handler = new CompletionHandlerFuture<>();
    	socket.disconnect(handler);
    	try {
			handler.getFuture().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
    }

	protected abstract void prepareChannel();

}
