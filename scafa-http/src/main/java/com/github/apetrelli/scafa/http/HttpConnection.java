package com.github.apetrelli.scafa.http;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.client.ClientConnection;

public interface HttpConnection extends ClientConnection {

    void sendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler);
    
    void sendData(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler);

    void end(CompletionHandler<Void, Void> completionHandler);
}
