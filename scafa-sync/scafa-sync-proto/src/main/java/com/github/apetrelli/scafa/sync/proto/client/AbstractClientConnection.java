package com.github.apetrelli.scafa.sync.proto.client;

import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.SyncSocketWrapper;

public abstract class AbstractClientConnection<T extends SyncSocket> extends SyncSocketWrapper<T> implements SyncSocket {

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
