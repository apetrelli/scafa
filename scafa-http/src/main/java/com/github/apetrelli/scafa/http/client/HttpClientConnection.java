package com.github.apetrelli.scafa.http.client;

import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpRequest;

public interface HttpClientConnection extends HttpConnection {

    void sendHeader(HttpRequest request, HttpClientHandler clientHandler);

}
