package com.github.apetrelli.scafa.proto.aio;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface BufferContextReader extends Closeable {

	CompletableFuture<CompletionHandlerResult<Integer, BufferContext>> read(BufferContext context);

	void close() throws IOException;
}
