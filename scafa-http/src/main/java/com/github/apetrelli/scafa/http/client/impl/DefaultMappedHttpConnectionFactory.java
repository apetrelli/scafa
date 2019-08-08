package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.aio.ResultHandler;

public class DefaultMappedHttpConnectionFactory implements MappedHttpConnectionFactory {

	@Override
	public void create(HttpRequest request, ResultHandler<HttpConnection> handler, HttpHandler responseHandler) {
		try {
			HttpConnection connection = new DirectHttpConnection(request.getHostPort(), responseHandler);
			connection.ensureConnected(new CompletionHandler<Void, Void>() {

				@Override
				public void completed(Void result, Void attachment) {
					handler.handle(connection);
				}

				@Override
				public void failed(Throwable exc, Void attachment) {
					handler.handle(new ThrowableHttpConnection(exc));
				}
			});
		} catch (IOException e) {
			handler.handle(new ThrowableHttpConnection(e));
		}
	}

	@Override
	public void disconnectAll() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose(HostPort target) {
		// TODO Auto-generated method stub

	}

}
