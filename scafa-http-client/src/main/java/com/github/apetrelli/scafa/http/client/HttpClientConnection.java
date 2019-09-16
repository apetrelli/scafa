package com.github.apetrelli.scafa.http.client;

import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.client.ClientConnection;
import com.github.apetrelli.scafa.proto.output.DataSender;

public interface HttpClientConnection extends ClientConnection {

    void sendHeader(HttpRequest request, HttpClientHandler clientHandler, CompletionHandler<Void, Void> completionHandler);

    DataSender createDataSender(HttpRequest request);
}
