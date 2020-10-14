/**
 * Scafa - A universal non-caching proxy for the road warrior
 * Copyright (C) 2015  Antonio Petrelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.apetrelli.scafa.http.proxy.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpHandler;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;

public class DefaultProxyHttpHandler implements ProxyHttpHandler {

    private MappedProxyHttpConnectionFactory connectionFactory;

    private AsyncSocket sourceChannel;

    private ProxyHttpConnection connection;

    public DefaultProxyHttpHandler(MappedProxyHttpConnectionFactory connectionFactory, AsyncSocket sourceChannel) {
        this.connectionFactory = connectionFactory;
        this.sourceChannel = sourceChannel;
    }

    @Override
    public void onConnect() throws IOException {
        // Does nothing
    }

    @Override
    public void onStart() {
        // Does nothing
    }

    @Override
    public CompletableFuture<Void> onResponseHeader(HttpResponse response) {
    	return CompletableFuture.failedFuture(new UnsupportedOperationException("Not expected a response header"));
    }

    @Override
    public CompletableFuture<Void> onRequestHeader(HttpRequest request) {
        if ("CONNECT".equalsIgnoreCase(request.getMethod())) {
            HttpConnectRequest connectRequest;
            try {
                connectRequest = new HttpConnectRequest(request);
                return onConnectMethod(connectRequest);
            } catch (IOException e) {
            	return CompletableFuture.failedFuture(e);
            }
        } else {
			return connectionFactory.create(sourceChannel, request).thenAccept(x -> connection = x)
					.thenCompose(x -> connection.sendHeader(request));
        }
    }
    
    @Override
    public CompletableFuture<Void> onBody(ByteBuffer buffer, long offset, long length) {
        return connection.sendData(buffer);
    }
    
    @Override
    public CompletableFuture<Void> onChunkStart(long totalOffset, long chunkLength) {
    	return CompletionHandlerFuture.completeEmpty();
    }
    
    @Override
    public CompletableFuture<Void> onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
        return connection.sendData(buffer);
    }

    @Override
    public CompletableFuture<Void> onChunkEnd() {
    	return CompletionHandlerFuture.completeEmpty();
    }

    @Override
    public CompletableFuture<Void> onChunkedTransferEnd() {
    	return CompletionHandlerFuture.completeEmpty();
    }
    
    @Override
    public CompletableFuture<Void> onConnectMethod(HttpConnectRequest connectRequest) {
		return connectionFactory.create(sourceChannel, connectRequest).thenAccept(x -> connection = x)
				.thenCompose(x -> connection.connect(connectRequest));
    }
    
    @Override
    public CompletableFuture<Void> onDataToPassAlong(ByteBuffer buffer) {
        return connection.flushBuffer(buffer);
    }
    
    @Override
    public CompletableFuture<Void> onEnd() {
        return connection.endData();
    }

    @Override
    public void onDisconnect() {
    	if (connection != null) {
    		connection.disconnect(); // Ignore the outcome
    	}
    }
}
