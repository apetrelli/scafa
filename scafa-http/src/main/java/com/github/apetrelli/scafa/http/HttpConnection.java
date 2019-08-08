package com.github.apetrelli.scafa.http;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public interface HttpConnection extends Closeable {

    void ensureConnected(CompletionHandler<Void, Void> handler);

    void sendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler);

    void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler);

    void end();
}
