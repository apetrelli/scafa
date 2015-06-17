package com.github.apetrelli.scafa.server.processor.http.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.server.processor.http.HttpConnection;
import com.github.apetrelli.scafa.server.processor.http.HttpConnectionFactory;
import com.github.apetrelli.scafa.server.processor.http.ProxyHttpHandler;

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
        connection.sendHeader(method, url, headers, httpVersion);
    }

    @Override
    public long onBody(ByteBuffer buffer, long offset, long length) throws IOException {
        int size = buffer.limit() - buffer.position();
        connection.send(buffer);
        return offset + size;
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
        connection.connect("CONNECT", host, port, headers, httpVersion);
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
        connection.close();
    }
}
