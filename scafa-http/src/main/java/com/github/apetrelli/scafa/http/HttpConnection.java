package com.github.apetrelli.scafa.http;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public interface HttpConnection extends Closeable {

    void ensureConnected(CompletionHandler<Void, Void> handler);

    void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler);

    void sendAsChunk(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler);

    void endChunkedTransfer(CompletionHandler<Void, Void> completionHandler);

    void end();
}
