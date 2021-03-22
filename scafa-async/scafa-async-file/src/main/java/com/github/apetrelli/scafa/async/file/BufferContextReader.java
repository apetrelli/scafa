package com.github.apetrelli.scafa.async.file;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface BufferContextReader extends Closeable {

	CompletableFuture<Integer> read(BufferContext context);

	void close() throws IOException;
}
