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
package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpConnectionFactory;
import com.github.apetrelli.scafa.http.ProxyHttpHandler;

public class DefaultProxyHttpHandler implements ProxyHttpHandler {

    private static final byte CR = 13;

    private static final byte LF = 10;

    private static final byte[] CRLF = new byte[] {CR, LF};

    private HttpConnectionFactory connectionFactory;

    private AsynchronousSocketChannel sourceChannel;

    private HttpConnection connection;

    private ByteBuffer countBuffer = ByteBuffer.allocate(256);

    public DefaultProxyHttpHandler(HttpConnectionFactory connectionFactory, AsynchronousSocketChannel sourceChannel) {
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
    public void onResponseHeader(String httpVersion, int responseCode, String responseMessage,
            Map<String, List<String>> headers) throws IOException {
        throw new UnsupportedOperationException("Not expected a response header");
    }

    @Override
    public void onRequestHeader(String method, String url, String httpVersion, Map<String, List<String>> headers)
            throws IOException {
        connection = connectionFactory.create(sourceChannel, method, url, headers, httpVersion);
        connection.sendHeader(method, url, httpVersion, headers);
    }

    @Override
    public void onBody(ByteBuffer buffer, long offset, long length) throws IOException {
        connection.send(buffer);
    }

    @Override
    public void onChunkStart(long totalOffset, long chunkLength) throws IOException {
        countBuffer.clear();
        countBuffer.put(Long.toHexString(chunkLength).getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF);
        countBuffer.flip();
        connection.send(countBuffer);
        countBuffer.clear();
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
    public void onConnectMethod(String host, int port, String httpVersion, Map<String, List<String>> headers)
            throws IOException {
        connection = connectionFactory.create(sourceChannel, "CONNECT", host, port, headers, httpVersion);
        connection.connect("CONNECT", host, port, httpVersion, headers);
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
