package com.github.apetrelli.scafa.proto.client;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public interface ClientConnection extends Closeable {

    void ensureConnected(CompletionHandler<Void, Void> handler);
    
    void disconnect(CompletionHandler<Void, Void> handler);

    void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler);

    void end();
}
