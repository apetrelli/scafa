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
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnection;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpHandler;
import com.github.apetrelli.scafa.http.proxy.ResultHandler;

public class DefaultProxyHttpHandler implements ProxyHttpHandler {

    private static final byte CR = 13;

    private static final byte LF = 10;

    private static final byte[] CRLF = new byte[] {CR, LF};

    private MappedHttpConnectionFactory connectionFactory;

    private AsynchronousSocketChannel sourceChannel;

    private HttpConnection connection;

    public DefaultProxyHttpHandler(MappedHttpConnectionFactory connectionFactory, AsynchronousSocketChannel sourceChannel) {
        this.connectionFactory = connectionFactory;
        this.sourceChannel = sourceChannel;
    }

    @Override
    public void onConnect() throws IOException {
        // Does nothing
    }

    @Override
    public void onStart() throws IOException {
        // Does nothing
    }

    @Override
    public void onResponseHeader(HttpResponse response) throws IOException {
        throw new UnsupportedOperationException("Not expected a response header");
    }

    @Override
    public void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler) {
        connectionFactory.create(sourceChannel, request, new ResultHandler<HttpConnection>() {

			@Override
			public void handle(HttpConnection result) {
				connection = result;
				connection.sendHeader(request, handler);
			}
		});
    }

    @Override
    public void onBody(ByteBuffer buffer, long offset, long length) throws IOException {
        connection.send(buffer);
    }

    @Override
    public void onChunkStart(long totalOffset, long chunkLength) throws IOException {

        String hexString = Long.toHexString(chunkLength);
        ByteBuffer countBuffer = ByteBuffer.allocate(hexString.length() + 2);
        countBuffer.put(hexString.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF);
        countBuffer.flip();
        connection.send(countBuffer);
    }

    @Override
    public void onChunk(byte[] buffer, int position, int length, long totalOffset, long chunkOffset, long chunkLength)
            throws IOException {
        connection.send(ByteBuffer.wrap(buffer, position, length));
    }

    @Override
    public void onChunkEnd() {
        // Does nothing.
    }

    @Override
    public void onChunkedTransferEnd() throws IOException {
        connection.send(ByteBuffer.wrap(CRLF));
    }

    @Override
    public void onConnectMethod(HttpConnectRequest connectRequest, CompletionHandler<Void, Void> handler) {
    	connectionFactory.create(sourceChannel, connectRequest, new ResultHandler<HttpConnection>() {

			@Override
			public void handle(HttpConnection result) {
				connection = result;
				connection.connect(connectRequest, handler);
			}
		});

    }

    @Override
    public void onDataToPassAlong(ByteBuffer buffer) throws IOException {
        connection.send(buffer);
    }

    @Override
    public void onEnd() throws IOException {
        connection.end();
    }

    @Override
    public void onDisconnect() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }
}
