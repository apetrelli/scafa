package com.github.apetrelli.scafa.proto.client.impl;

import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;
import com.github.apetrelli.scafa.proto.aio.impl.AsyncSocketWrapper;

public abstract class AbstractClientConnection<T extends AsyncSocket> extends AsyncSocketWrapper<T> implements AsyncSocket {

    public AbstractClientConnection(T socket) {
    	super(socket);
	}

	@Override
    public void connect(CompletionHandler<Void, Void> handler) {
		socket.connect(new DelegateFailureCompletionHandler<Void, Void>(handler) {

			@Override
			public void completed(Void result, Void attachment) {
                prepareChannel();
                handler.completed(result, attachment);
			}
		});
    }

	protected abstract void prepareChannel();

}
