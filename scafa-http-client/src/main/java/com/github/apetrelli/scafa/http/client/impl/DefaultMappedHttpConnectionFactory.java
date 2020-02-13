package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.impl.DirectHttpAsyncSocket;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsynchronousSocketChannelFactory;
import com.github.apetrelli.scafa.proto.aio.IgnoringCompletionHandler;
import com.github.apetrelli.scafa.proto.aio.impl.DirectClientAsyncSocket;
import com.github.apetrelli.scafa.proto.aio.impl.SimpleAsynchronousSocketChannelFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DefaultMappedHttpConnectionFactory implements MappedHttpConnectionFactory {

    private DataSenderFactory dataSenderFactory;
    
    private AsynchronousSocketChannelFactory channelFactory = new SimpleAsynchronousSocketChannelFactory();

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
			    AsyncSocket socket = new DirectClientAsyncSocket(channelFactory, hostPort, null, false);
			    HttpAsyncSocket httpSocket = new DirectHttpAsyncSocket(socket, dataSenderFactory);
				HttpClientConnection connection = new DirectHttpConnection(httpSocket, this);
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
	    CompletionHandler<Void, Void> handler = new IgnoringCompletionHandler<>();
        connectionCache.values().stream().forEach(t -> t.disconnect(handler));
        connectionCache.clear();
	}

	@Override
	public void dispose(HostPort target) {
		connectionCache.remove(target);
	}
}
