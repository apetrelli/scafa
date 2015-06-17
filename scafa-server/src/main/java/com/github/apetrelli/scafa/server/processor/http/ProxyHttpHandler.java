package com.github.apetrelli.scafa.server.processor.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public interface ProxyHttpHandler extends HttpHandler {

    void onConnectMethod(String host, int port, String httpVersion, Map<String, List<String>> headers)
            throws IOException;
    
    void onDataToPassAlong(ByteBuffer buffer) throws IOException;
}
