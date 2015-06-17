/**
 * Scafa - Universal roadwarrior non-caching proxy
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
package com.github.apetrelli.scafa.server.processor.http.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.server.processor.http.HttpBodyMode;
import com.github.apetrelli.scafa.server.processor.http.HttpByteSink;
import com.github.apetrelli.scafa.server.processor.http.HttpInput;
import com.github.apetrelli.scafa.server.processor.http.ProxyHttpHandler;

public class DefaultHttpByteSink implements HttpByteSink {

    private final static Logger LOG = Logger
            .getLogger(DefaultHttpByteSink.class.getName());

    private static final String HTTP_502 = "HTTP/1.1 502 Bad gateway \r\n"
            + "Connection: close\r\n"
            + "Content-Length: %1$d\r\n\r\n";

    private AsynchronousSocketChannel sourceChannel;
    
    private ProxyHttpHandler handler;

    private byte[] buffer = new byte[16384];

    private byte[] chunkCountBuffer = new byte[256];

    private int bufferCount = 0, offset = 0;

    private int chunkCountBufferCount = 0;

    private String method, url, httpVersion;

    private Map<String, List<String>> headers = new LinkedHashMap<>();

    private byte[] generic502Page;

    public DefaultHttpByteSink(AsynchronousSocketChannel sourceChannel, ProxyHttpHandler handler) {
        this.sourceChannel = sourceChannel;
        this.handler = handler;
        try {
            generic502Page = loadResource("/errorpages/generic-502.html");
        } catch (IOException e) {
            throw new IllegalStateException("Error page not found", e);
        }
    }

    @Override
    public HttpInput createInput() {
        HttpInput retValue = new HttpInput();
        ByteBuffer buffer = ByteBuffer.allocate(16384);
        retValue.setBuffer(buffer);
        return retValue;
    }

    @Override
    public void start(HttpInput input) {
        buffer[0] = input.getBuffer().get();
        bufferCount = 1;
        method = null;
        url = null;
        httpVersion = null;
        headers.clear();
    }

    @Override
    public void appendRequestLine(byte currentByte) {
        appendToBuffer(currentByte);
    }

    @Override
    public void endRequestLine(byte currentByte) {
        String requestLine = new String(buffer, 0, bufferCount,
                StandardCharsets.US_ASCII);
        String[] pieces = requestLine.split("\\s+");
        if (pieces.length == 3) {
            method = pieces[0];
            url = pieces[1];
            httpVersion = pieces[2];
        } else {
            LOG.severe("The request line is invalid: " + requestLine);
        }
        appendToBuffer(currentByte);
    }

    @Override
    public void beforeHeader(byte currentByte) {
        appendToBuffer(currentByte);
        offset = bufferCount;
    }

    @Override
    public void beforeHeaderLine(byte currentByte) {
        appendToBuffer(currentByte);
        offset = bufferCount;
    }

    @Override
    public void appendHeader(byte currentByte) {
        appendToBuffer(currentByte);
    }

    @Override
    public void endHeaderLine(byte currentByte) {
        String header = new String(buffer, offset, bufferCount - offset, StandardCharsets.US_ASCII);
        int pos = header.indexOf(": ");
        if (pos > 0) {
            String key = header.substring(0, pos).toUpperCase();
            String value = header.substring(pos + 2, header.length());
            List<String> values = headers.get(key);
            if (values == null) {
                values = new ArrayList<>();
                headers.put(key, values);
            }
            values.add(value);
        } else {
            LOG.severe("The header is invalid: " + header);
        }
        appendToBuffer(currentByte);
    }

    @Override
    public void preEndHeader(HttpInput input) {
        appendToBuffer(input.getBuffer().get());
        input.setBodyMode(HttpBodyMode.EMPTY);
        List<String> contentLengths = headers.get("CONTENT-LENGTH");
        if (contentLengths != null) { // Check for body
            if (contentLengths.size() == 1) {
                String lengthString = contentLengths.iterator().next();
                try {
                    long length = Long.parseLong(lengthString);
                    input.setBodyMode(HttpBodyMode.BODY);
                    input.setBodySize(length);
                } catch (NumberFormatException e) {
                    LOG.log(Level.SEVERE, "The provided length is not an integer: " + lengthString, e);
                }
            } else {
                LOG.severe("Multiple content lengths: " + contentLengths.toString());
            }
        } else { // Check chunked transfer
            List<String> encodings = headers.get("TRANSFER-ENCODING");
            if (encodings != null) { // Check for body
                if (encodings.size() == 1) {
                    if (encodings.iterator().next().equals("chunked")) {
                        input.setBodyMode(HttpBodyMode.CHUNKED);
                    }
                } else {
                    LOG.log(Level.SEVERE, "Multiple transfer encodings: " + encodings.toString());
                }
            }
        }
    }

    @Override
    public void endHeader(HttpInput input) throws IOException {
        appendToBuffer(input.getBuffer().get());
        chunkCountBufferCount = 0;
        if (LOG.isLoggable(Level.FINEST)) {
            String request = new String(buffer, 0, bufferCount);
            LOG.finest("-- Request sent to " + url + " --");
            LOG.finest(request);
            LOG.finest("-- End of request --");
        }
        if ("CONNECT".equalsIgnoreCase(method)) {
            String[] strings = url.split(":");
            if (strings.length != 2) {
                throw new IOException("Invalid host:port " + url);
            }
            try {
                int port = Integer.parseInt(strings[1]);
                handler.onConnectMethod(strings[0], port, httpVersion, headers);
                input.setHttpConnected(true);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid port in host:port " + url, e);
            }
        } else {
            handler.onRequestHeader(method, url, httpVersion, headers);
        }
    }

    @Override
    public void endHeaderAndRequest(HttpInput input) throws IOException {
        try {
            endHeader(input);
        } catch (IOException | RuntimeException e) {
            sendErrorPage(sourceChannel, HTTP_502, generic502Page);
            throw e;
        }
    }

    @Override
    public void beforeChunkCount(byte currentByte) throws IOException {
        chunkCountBufferCount = 0;
    }

    @Override
    public void appendChunkCount(byte currentByte) throws IOException {
        chunkCountBuffer[chunkCountBufferCount] = currentByte;
    }

    @Override
    public void endChunkCount(HttpInput input) throws IOException {
        if (chunkCountBufferCount > 0) {
            String chunkCountHex = new String(chunkCountBuffer, 0, chunkCountBufferCount, StandardCharsets.US_ASCII);
            try {
                long chunkCount = Long.parseLong(chunkCountHex, 16);
                input.setChunkLength(chunkCount);
                if (!input.isCaughtError()) {
                    handler.onChunkStart(input.getTotalChunkedTransferLength(), chunkCount);
                }
                if (chunkCount == 0L) {
                    if (!input.isCaughtError()) {
                        handler.onChunkEnd();
                        handler.onChunkedTransferEnd();
                        handler.onEnd();
                    } else {
                        sendErrorPage(sourceChannel, HTTP_502, generic502Page);
                    }
                }
            } catch (NumberFormatException e) {
                LOG.log(Level.SEVERE, "Chunk count invalid for an hex value: " + chunkCountHex, e);
            }
        } else {
            LOG.severe("Chunk count empty, invalid");
        }
    }

    @Override
    public void send(HttpInput input) throws IOException {
        int size = input.getBuffer().limit() - input.getBuffer().position();
        if (!input.isCaughtError()) {
            handler.onBody(input.getBuffer(), input.getBodyOffset(), input.getBodySize());
        }
        input.reduceBody(size);
        if (!input.isHttpConnected() && input.isCaughtError() && input.getCountdown() <= 0L) {
            sendErrorPage(sourceChannel, HTTP_502, generic502Page);
        }
    }
    
    @Override
    public void sendChunkData(HttpInput input) throws IOException {
        if (!input.isCaughtError()) {
            long bufferSize = input.getBuffer().limit() - input.getBuffer().position();
            long maxsize = input.getCountdown();
            if (bufferSize > maxsize) {
                bufferSize = maxsize;
            }
            handler.onChunk(input.getBuffer().array(), input.getBuffer().position(), new Long(bufferSize).intValue(),
                    input.getTotalChunkedTransferLength() - input.getChunkLength(), input.getChunkOffset(),
                    input.getChunkLength());
            input.reduceChunk(bufferSize);
        }
    }

    private void appendToBuffer(byte currentByte) {
        buffer[bufferCount] = currentByte;
        bufferCount++;
    }

    private byte[] loadResource(String resourcePath) throws IOException {
        byte[] retValue;
        try (InputStream stream = getClass().getResourceAsStream(resourcePath);
                ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int amount;
            while ((amount = stream.read(buf)) >= 0) {
                os.write(buf, 0, amount);
            }
            retValue = os.toByteArray();
        }
        return retValue;
    }

    private void sendErrorPage(AsynchronousSocketChannel client, String header,
            byte[] page) {
        String realHeader = String.format(header, page.length);
        try {
            client.write(ByteBuffer.wrap(realHeader.getBytes(StandardCharsets.US_ASCII))).get();
            client.write(ByteBuffer.wrap(page)).get();
            client.close();
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOG.log(Level.SEVERE, "Error when sending error page", e);
        }
    }

    @Override
    public void disconnect() throws IOException {
        handler.onDisconnect();
    }
}
