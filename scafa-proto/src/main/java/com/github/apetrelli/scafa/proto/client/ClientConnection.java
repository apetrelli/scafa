package com.github.apetrelli.scafa.proto.client;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public interface ClientConnection {

    void connect(CompletionHandler<Void, Void> handler);
    
    void disconnect(CompletionHandler<Void, Void> handler);

    void flushBuffer(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler);
}
