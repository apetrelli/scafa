package com.github.apetrelli.scafa.http.server.sync;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;

public interface HttpServerHandlerFactory {

	HttpServerHandler create(HttpSyncSocket<HttpResponse> channel);
}
