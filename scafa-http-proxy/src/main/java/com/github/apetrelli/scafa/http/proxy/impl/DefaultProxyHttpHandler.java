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
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpHandler;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.IgnoringCompletionHandler;

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
    public void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> handler) {
        handler.failed(new UnsupportedOperationException("Not expected a response header"), null);
    }

    @Override
    public void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler) {
        if ("CONNECT".equalsIgnoreCase(request.getMethod())) {
            HttpConnectRequest connectRequest;
            try {
                connectRequest = new HttpConnectRequest(request);
                onConnectMethod(connectRequest, handler);
            } catch (IOException e) {
                handler.failed(e, null);
            }
        } else {
            connectionFactory.create(sourceChannel, request, new CompletionHandler<ProxyHttpConnection, Void>() {

                @Override
                public void completed(ProxyHttpConnection result, Void attachment) {
                    connection = result;
                    connection.sendHeader(request, handler);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    handler.failed(exc, attachment);
                }
            });
        }
    }

    @Override
    public void onBody(ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler) {
        connection.sendData(buffer, handler);
    }

    @Override
    public void onChunkStart(long totalOffset, long chunkLength, CompletionHandler<Void, Void> handler) {
    	// Does nothing
    }

    @Override
    public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength,
            CompletionHandler<Void, Void> handler) {
        connection.sendData(buffer, handler);
    }

    @Override
    public void onChunkEnd(CompletionHandler<Void, Void> handler) {
    	// Does nothing
    }

    @Override
    public void onChunkedTransferEnd(CompletionHandler<Void, Void> handler) {
        // Does nothing
    }

    @Override
    public void onConnectMethod(HttpConnectRequest connectRequest, CompletionHandler<Void, Void> handler) {
        connectionFactory.create(sourceChannel, connectRequest, new CompletionHandler<ProxyHttpConnection, Void>() {

            @Override
            public void completed(ProxyHttpConnection result, Void attachment) {
                connection = result;
                connection.connect(connectRequest, handler);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                handler.failed(exc, attachment);
            }
        });

    }

    @Override
    public void onDataToPassAlong(ByteBuffer buffer, CompletionHandler<Void, Void> handler) {
        connection.flushBuffer(buffer, handler);
    }

    @Override
    public void onEnd(CompletionHandler<Void, Void> handler) {
        connection.endData(handler);
    }

    @Override
    public void onDisconnect() {
    	if (connection != null) {
    		connection.disconnect(new IgnoringCompletionHandler<>());
    	}
    }
}
