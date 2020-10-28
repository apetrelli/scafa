package com.github.apetrelli.scafa.proto.sync;

import java.io.IOException;

public interface SyncServerSocketFactory<T extends SyncSocket> {
	
	SyncServerSocket<T> create() throws IOException;
}
