package com.github.apetrelli.scafa.sync.http.server;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;

public interface HttpServerHandlerFactory {

	HttpServerHandler create(HttpSyncSocket<HttpResponse> channel);
}
