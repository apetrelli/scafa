package com.github.apetrelli.scafa.proto.sync;

import java.io.Closeable;

public interface SyncServerSocket<T extends SyncSocket> extends Closeable {

	T accept();
}
