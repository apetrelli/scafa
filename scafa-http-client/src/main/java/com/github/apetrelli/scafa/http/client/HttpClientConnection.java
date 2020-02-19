package com.github.apetrelli.scafa.http.client;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;

public interface HttpClientConnection extends HttpAsyncSocket<HttpRequest> {

    void prepare(HttpRequest request, HttpClientHandler clientHandler);
}
