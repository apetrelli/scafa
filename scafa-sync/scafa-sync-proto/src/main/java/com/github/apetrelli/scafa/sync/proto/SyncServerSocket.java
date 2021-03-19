package com.github.apetrelli.scafa.sync.proto;

import java.io.Closeable;

public interface SyncServerSocket<T extends SyncSocket> extends Closeable {

	T accept();
	
	boolean isOpen();
}
