package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.HttpHandler;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.http.socket.direct.DirectHttpAsyncSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultMappedHttpConnectionFactory implements MappedHttpConnectionFactory {

    private final DataSenderFactory dataSenderFactory;
    
    private final SocketFactory<AsyncSocket> socketFactory;
    
    private final ProcessorFactory<HttpHandler, AsyncSocket> processorFactory;

    private final Map<HostPort, HttpClientConnection> connectionCache = new HashMap<>();
	
	@Override
	public CompletableFuture<HttpClientConnection> create(HttpRequest request) {
		try {
			HostPort hostPort = request.getHostPort();
			HttpClientConnection cachedConnection = connectionCache.get(hostPort);
			if (cachedConnection == null) {
			    AsyncSocket socket = socketFactory.create(hostPort, null, false);
			    HttpAsyncSocket<HttpRequest> httpSocket = new DirectHttpAsyncSocket<>(socket, dataSenderFactory);
				HttpClientConnection connection = new DirectHttpConnection(httpSocket, this, processorFactory);
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
