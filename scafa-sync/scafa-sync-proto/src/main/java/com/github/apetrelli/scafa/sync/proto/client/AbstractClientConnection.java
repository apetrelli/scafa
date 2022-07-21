package com.github.apetrelli.scafa.sync.proto.client;

import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.sync.proto.SyncSocketWrapper;

public abstract class AbstractClientConnection<T extends Socket> extends SyncSocketWrapper<T> implements Socket {

    public AbstractClientConnection(T socket) {
    	super(socket);
	}
    
    @Override
    public void connect() {
    	super.connect();
    	prepareChannel();
    }

	protected abstract void prepareChannel();

}
