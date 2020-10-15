package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.impl.DirectHttpAsyncSocket;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsynchronousSocketChannelFactory;
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
	public CompletableFuture<HttpClientConnection> create(HttpRequest request) {
		try {
			HostPort hostPort = request.getHostPort();
			HttpClientConnection cachedConnection = connectionCache.get(hostPort);
			if (cachedConnection == null) {
			    AsyncSocket socket = new DirectClientAsyncSocket(channelFactory, hostPort, null, false);
			    HttpAsyncSocket<HttpRequest> httpSocket = new DirectHttpAsyncSocket<>(socket, dataSenderFactory);
				HttpClientConnection connection = new DirectHttpConnection(httpSocket, this);
				connectionCache.put(hostPort, connection);
				return connection.connect().thenApply(x -> connection);
			} else {
				return CompletableFuture.completedFuture(cachedConnection);
			}
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public void disconnectAll() {
        connectionCache.values().stream().forEach(AsyncSocket::disconnect);
        connectionCache.clear();
	}

	@Override
	public void dispose(HostPort target) {
		connectionCache.remove(target);
	}
}
