package com.github.apetrelli.scafa.proto.sync.client;

import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.SyncSocketWrapper;

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
