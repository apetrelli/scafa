package com.github.apetrelli.scafa.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public interface HttpHandler {

    void onConnect() throws IOException;

    void onStart() throws IOException;

    void onResponseHeader(String httpVersion, int responseCode, String responseMessage,
            Map<String, List<String>> headers) throws IOException;

    void onRequestHeader(String method, String url, String httpVersion, Map<String, List<String>> headers)
            throws IOException;

    void onBody(ByteBuffer buffer, long offset, long length) throws IOException;

    void onChunkStart(long totalOffset, long chunkLength) throws IOException;

    void onChunk(byte[] buffer, int position, int length, long totalOffset, long chunkOffset, long chunkLength)
            throws IOException;

    void onChunkEnd() throws IOException;

    void onChunkedTransferEnd() throws IOException;

    void onEnd() throws IOException;

    void onDisconnect() throws IOException;
}
