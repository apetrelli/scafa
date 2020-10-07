package com.github.apetrelli.scafa.proto.client.impl;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.impl.AsyncSocketWrapper;

public abstract class AbstractClientConnection<T extends AsyncSocket> extends AsyncSocketWrapper<T> implements AsyncSocket {

    public AbstractClientConnection(T socket) {
    	super(socket);
	}

	@Override
    public void connect() {
		socket.connect();
        prepareChannel();
    }

	protected abstract void prepareChannel();

}
