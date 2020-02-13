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
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.IgnoringCompletionHandler;

public class DefaultGatewayHttpHandler implements HttpHandler {

    private MappedGatewayHttpConnectionFactory connectionFactory;

    private AsyncSocket sourceChannel;

    private HttpConnection connection;

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
    public void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> handler) {
        handler.failed(new UnsupportedOperationException("Not expected a response header"), null);
    }

    @Override
    public void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler) {
        connectionFactory.create(sourceChannel, request, new CompletionHandler<HttpConnection, Void>() {

            @Override
            public void completed(HttpConnection result, Void attachment) {
                connection = result;
                connection.sendHeader(request, handler);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                handler.failed(exc, attachment);
            }
        });
    }

    @Override
    public void onBody(ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler) {
        connection.sendData(buffer, handler);
    }

    @Override
    public void onChunkStart(long totalOffset, long chunkLength, CompletionHandler<Void, Void> handler) {
    	// Do nothing
    }

    @Override
    public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength,
            CompletionHandler<Void, Void> handler) {
        connection.sendData(buffer, handler);
    }

    @Override
    public void onChunkEnd(CompletionHandler<Void, Void> handler) {
    	// Do nothing
    }

    @Override
    public void onChunkedTransferEnd(CompletionHandler<Void, Void> handler) {
    	// Do nothing
    }

    @Override
    public void onDataToPassAlong(ByteBuffer buffer, CompletionHandler<Void, Void> handler) {
        connection.send(buffer, handler);
    }

    @Override
    public void onEnd(CompletionHandler<Void, Void> handler) {
        connection.end(handler);
    }

    @Override
    public void onDisconnect() {
        if (connection != null) {
            connection.disconnect(IgnoringCompletionHandler.INSTANCE);
        }
    }
}
