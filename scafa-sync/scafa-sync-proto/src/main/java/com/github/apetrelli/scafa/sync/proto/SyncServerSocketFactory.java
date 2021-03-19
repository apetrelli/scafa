package com.github.apetrelli.scafa.sync.proto;

import java.io.IOException;

public interface SyncServerSocketFactory<T extends SyncSocket> {
	
	SyncServerSocket<T> create() throws IOException;
}
