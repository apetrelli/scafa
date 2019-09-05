package com.github.apetrelli.scafa.http.client;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.CompletionHandler;

public interface BufferContextReader extends Closeable {

	void read(BufferContext context, CompletionHandler<Integer, BufferContext> completionHandler);

	void close() throws IOException;
}
