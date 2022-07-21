package com.github.apetrelli.scafa.sync.proto;

import java.io.Closeable;

import com.github.apetrelli.scafa.proto.Socket;

public interface SyncServerSocket<T extends Socket> extends Closeable {

	T accept();
	
	boolean isOpen();
}
