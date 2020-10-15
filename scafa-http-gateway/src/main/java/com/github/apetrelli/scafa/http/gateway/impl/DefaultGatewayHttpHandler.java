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
package com.github.apetrelli.scafa.http.gateway.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;

public class DefaultGatewayHttpHandler implements HttpHandler {

    private MappedGatewayHttpConnectionFactory connectionFactory;

    private AsyncSocket sourceChannel;

    private HttpAsyncSocket<HttpRequest> connection;

    public DefaultGatewayHttpHandler(MappedGatewayHttpConnectionFactory connectionFactory, AsyncSocket sourceChannel) {
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
    	return connectionFactory.create(sourceChannel, request).thenCompose(result -> {
    		connection = result;
    		return connection.sendHeader(request);
    	});
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
            connection.disconnect();
        }
    }
}
