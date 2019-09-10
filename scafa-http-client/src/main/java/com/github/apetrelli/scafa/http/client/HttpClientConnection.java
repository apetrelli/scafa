package com.github.apetrelli.scafa.http.client;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.impl.internal.DataSender;

public interface HttpClientConnection extends HttpConnection {

    void sendHeader(HttpRequest request, HttpClientHandler clientHandler, CompletionHandler<Void, Void> completionHandler);

    void sendAsChunk(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler);

    void endChunkedTransfer(CompletionHandler<Void, Void> completionHandler);

    DataSender createDataSender(HttpRequest request);
}
