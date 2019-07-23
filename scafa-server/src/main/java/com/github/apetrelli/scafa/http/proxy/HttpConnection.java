package com.github.apetrelli.scafa.http.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpRequest;

public interface HttpConnection {

    void ensureConnected(CompletionHandler<Void, Void> handler);

    void sendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler);

    void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler);

    void end();

    void close() throws IOException;

}