package com.github.apetrelli.scafa.async.proto.client;

import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocketWrapper;

public abstract class AbstractClientConnection<T extends AsyncSocket> extends AsyncSocketWrapper<T> implements AsyncSocket {

    public AbstractClientConnection(T socket) {
    	super(socket);
	}
    
    @Override
    public CompletableFuture<Void> connect() {
    	return super.connect().thenApply(x -> {
    		prepareChannel();
    		return x;
    	});
    }

	protected abstract void prepareChannel();

}
