package com.github.apetrelli.scafa.http.client;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;

public interface HttpClientConnection extends HttpAsyncSocket<HttpRequest> {

    void prepare(HttpRequest request, HttpClientHandler clientHandler);
}
