package com.github.apetrelli.scafa.sync.proto;

import java.io.IOException;

import com.github.apetrelli.scafa.proto.Socket;

public interface SyncServerSocketFactory<T extends Socket> {
	
	SyncServerSocket<T> create() throws IOException;
}
