package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.aio.DirectClientAsyncSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.async.HttpHandler;
import com.github.apetrelli.scafa.http.async.direct.DirectHttpAsyncSocket;
import com.github.apetrelli.scafa.http.async.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class DefaultMappedHttpConnectionFactory implements MappedHttpConnectionFactory {

    private DataSenderFactory dataSenderFactory;
    
    private SocketFactory<AsyncSocket> socketFactory = new DirectClientAsyncSocketFactory();
    
    private ProcessorFactory<HttpHandler, AsyncSocket> processorFactory;

    private Map<HostPort, HttpClientConnection> connectionCache = new HashMap<>();

	public DefaultMappedHttpConnectionFactory(DataSenderFactory dataSenderFactory,
			SocketFactory<AsyncSocket> socketFactory, ProcessorFactory<HttpHandler, AsyncSocket> processorFactory) {
        this.dataSenderFactory = dataSenderFactory;
        this.socketFactory = socketFactory;
        this.processorFactory = processorFactory;
    }
	
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
