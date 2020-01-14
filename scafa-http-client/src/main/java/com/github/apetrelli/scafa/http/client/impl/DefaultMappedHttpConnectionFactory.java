package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.tls.util.IOUtils;

public class DefaultMappedHttpConnectionFactory implements MappedHttpConnectionFactory {

    private DataSenderFactory dataSenderFactory;

    private Map<HostPort, HttpClientConnection> connectionCache = new HashMap<>();

	public DefaultMappedHttpConnectionFactory(DataSenderFactory dataSenderFactory) {
        this.dataSenderFactory = dataSenderFactory;
    }

    @Override
	public void create(HttpRequest request, CompletionHandler<HttpClientConnection, Void> handler) {
		try {
			HostPort hostPort = request.getHostPort();
			HttpClientConnection cachedConnection = connectionCache.get(hostPort);
			if (cachedConnection == null) {
				HttpClientConnection connection = new DirectHttpConnection(hostPort, this, dataSenderFactory);
				connectionCache.put(hostPort, connection);
				connection.ensureConnected(new CompletionHandler<Void, Void>() {

					@Override
					public void completed(Void result, Void attachment) {
						handler.completed(connection, attachment);
					}

					@Override
					public void failed(Throwable exc, Void attachment) {
						handler.failed(exc, attachment);
					}
				});
			} else {
				handler.completed(cachedConnection, null);
			}
		} catch (IOException e) {
			handler.failed(e, null);
		}
	}

	@Override
	public void disconnectAll() {
        connectionCache.values().stream().forEach(t -> IOUtils.closeQuietly(t));
        connectionCache.clear();
	}

	@Override
	public void dispose(HostPort target) {
		connectionCache.remove(target);
	}
}
