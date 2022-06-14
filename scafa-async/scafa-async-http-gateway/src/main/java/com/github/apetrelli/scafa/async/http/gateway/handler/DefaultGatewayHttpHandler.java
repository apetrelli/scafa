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
package com.github.apetrelli.scafa.async.http.gateway.handler;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;

import lombok.RequiredArgsConstructor;

import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.HttpHandler;
import com.github.apetrelli.scafa.async.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.impl.HttpHandlerSupport;

@RequiredArgsConstructor
public class DefaultGatewayHttpHandler<T extends HttpAsyncSocket<HttpRequest>> extends HttpHandlerSupport implements HttpHandler {
    
    protected final ByteBuffer writeBuffer = ByteBuffer.allocate(16384);

    private final MappedGatewayHttpConnectionFactory<T> connectionFactory;

    private final AsyncSocket sourceChannel;

    private T connection;
    
    @Override
    public CompletableFuture<Void> onResponseHeader(HttpResponse response) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not expected a response header"));
    }

    @Override
    public CompletableFuture<Void> onRequestHeader(HttpRequest request) {
		return createConnection(request).thenCompose(x -> x.sendHeader(request, writeBuffer));
    }
    
    @Override
    public CompletableFuture<Void> onBody(ByteBuffer buffer, long offset, long length) {
        return connection.sendData(buffer);
    }
    
    @Override
    public CompletableFuture<Void> onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
        return connection.sendData(buffer);
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

	protected CompletableFuture<T> createConnection(HttpRequest request) {
		return connectionFactory.create(sourceChannel, request).thenApply(x -> connection = x);
	}
}
