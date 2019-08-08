package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.aio.ResultHandler;

public class DefaultMappedHttpConnectionFactory implements MappedHttpConnectionFactory {

	private static final Logger LOG = Logger.getLogger(DefaultMappedHttpConnectionFactory.class.getName());

    private Map<HostPort, HttpConnection> connectionCache = new HashMap<>();

	@Override
	public void create(HttpRequest request, ResultHandler<HttpConnection> handler, HttpHandler responseHandler) {
		try {
			HostPort hostPort = request.getHostPort();
			HttpConnection cachedConnection = connectionCache.get(hostPort);
			if (cachedConnection == null) {
				HttpConnection connection = new DirectHttpConnection(hostPort, responseHandler, this);
				connectionCache.put(hostPort, connection);
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
			} else {
				handler.handle(cachedConnection);
			}
		} catch (IOException e) {
			handler.handle(new ThrowableHttpConnection(e));
		}
	}

	@Override
	public void disconnectAll() {
        connectionCache.values().stream().forEach(t -> closeQuietly(t));
        connectionCache.clear();
	}

	@Override
	public void dispose(HostPort target) {
		connectionCache.remove(target);
	}

	private void closeQuietly(HttpConnection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Cannot close connection", e);
			}
		}
	}
}
