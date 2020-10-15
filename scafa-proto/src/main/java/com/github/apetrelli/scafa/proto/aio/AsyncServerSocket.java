package com.github.apetrelli.scafa.proto.aio;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface AsyncServerSocket<T extends AsyncSocket> extends Closeable {

	CompletableFuture<T> accept();
}
