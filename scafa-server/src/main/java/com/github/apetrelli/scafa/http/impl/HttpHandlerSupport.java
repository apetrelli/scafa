package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.http.HttpHandler;

public class HttpHandlerSupport implements HttpHandler {

    @Override
    public void onConnect() throws IOException {
    }

    @Override
    public void onStart() throws IOException {
    }

    @Override
    public void onResponseHeader(String httpVersion, int responseCode, String responseMessage,
            Map<String, List<String>> headers) throws IOException {
    }

    @Override
    public void onRequestHeader(String method, String url, String httpVersion, Map<String, List<String>> headers)
            throws IOException {
    }

    @Override
    public void onBody(ByteBuffer buffer, long offset, long length) throws IOException {
        buffer.position(buffer.limit());
    }

    @Override
    public void onChunkStart(long totalOffset, long chunkLength) throws IOException {
    }

    @Override
    public void onChunk(byte[] buffer, int position, int length, long totalOffset, long chunkOffset, long chunkLength)
            throws IOException {
    }

    @Override
    public void onChunkEnd() {
    }

    @Override
    public void onChunkedTransferEnd() throws IOException {
    }

    @Override
    public void onEnd() throws IOException {
    }

    @Override
    public void onDisconnect() throws IOException {
    }

}
