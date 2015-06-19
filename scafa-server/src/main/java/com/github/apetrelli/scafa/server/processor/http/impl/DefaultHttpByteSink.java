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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.server.processor.http.HttpBodyMode;
import com.github.apetrelli.scafa.server.processor.http.HttpByteSink;
import com.github.apetrelli.scafa.server.processor.http.HttpHandler;
import com.github.apetrelli.scafa.server.processor.http.HttpInput;

public class DefaultHttpByteSink<H extends HttpHandler> implements HttpByteSink {

    private final static Logger LOG = Logger
            .getLogger(DefaultHttpByteSink.class.getName());
    
    protected H handler;

    private byte[] buffer = new byte[16384];

    private byte[] chunkCountBuffer = new byte[256];

    private int bufferCount = 0, offset = 0;

    private int chunkCountBufferCount = 0;

    private String method, url, httpVersion, responseMessage;
    
    private int responseCode = 0;
    
    private boolean isRequest = true;

    private Map<String, List<String>> headers = new LinkedHashMap<>();

    public DefaultHttpByteSink(H handler) {
        this.handler = handler;
    }
    
    @Override
    public void connect() throws IOException {
        handler.onConnect();
    }

    @Override
    public HttpInput createInput() {
        HttpInput retValue = new HttpInput();
        ByteBuffer buffer = ByteBuffer.allocate(16384);
        retValue.setBuffer(buffer);
        return retValue;
    }
    
    @Override
    public void reset() {
        bufferCount = 0;
        method = null;
        url = null;
        httpVersion = null;
        headers.clear();
        isRequest = true;
        responseMessage = null;
        responseCode = 0;
    }

    @Override
    public void start(HttpInput input) {
        buffer[0] = input.getBuffer().get();
        bufferCount = 1;
        method = null;
        url = null;
        httpVersion = null;
        headers.clear();
        isRequest = true;
        responseMessage = null;
        responseCode = 0;
    }

    @Override
    public void appendRequestLine(byte currentByte) {
        appendToBuffer(currentByte);
    }

    @Override
    public void endRequestLine(HttpInput input) {
        byte currentByte = input.getBuffer().get();
        String requestLine = new String(buffer, 0, bufferCount,
                StandardCharsets.US_ASCII);
        String[] pieces = requestLine.split("\\s+");
        if (pieces.length >= 3) {
            if (pieces[0].startsWith("HTTP/")) {
                isRequest = false;
                httpVersion = pieces[0];
                try {
                    responseCode = Integer.parseInt(pieces[1]);
                } catch (NumberFormatException e) {
                    responseCode = -1;
                    LOG.log(Level.SEVERE, "The response code is not a number: " + pieces[1], e);
                }
                StringBuilder builder = new StringBuilder();
                builder.append(pieces[2]);
                for (int i = 3; i < pieces.length; i++) {
                    builder.append(" ").append(pieces[i]);
                }
                responseMessage = builder.toString();
            } else if (pieces.length == 3) {
                method = pieces[0];
                url = pieces[1];
                httpVersion = pieces[2];
            } else {
                input.setCaughtError(true);
                LOG.severe("The request line is invalid: " + requestLine);
            }
        } else {
            input.setCaughtError(true);
            LOG.severe("The request or response line is invalid: " + requestLine);
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
                    long length = Long.parseLong(lengthString.trim());
                    if (length > 0L) {
                        input.setBodyMode(HttpBodyMode.BODY);
                        input.setBodySize(length);
                    }
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
        if (isRequest) {
            manageRequestheader(handler, input, method, url, httpVersion, headers);
        } else {
            manageResponseHeader(handler, input, httpVersion, responseCode, responseMessage, headers);
        }
    }

    @Override
    public void endHeaderAndRequest(HttpInput input) throws IOException {
        try {
            endHeader(input);
            handler.onEnd();
        } catch (IOException | RuntimeException e) {
            input.setCaughtError(true);
            manageError();
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
                        manageError();
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
        if (!input.isHttpConnected() && input.getCountdown() <= 0L) {
            handler.onEnd();
            if (input.isCaughtError()) {
                manageError();
            }
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

    @Override
    public void disconnect() throws IOException {
        handler.onDisconnect();
    }

    protected void manageResponseHeader(H handler, HttpInput input, String httpVersion, int responseCode, String responseMessage,
            Map<String, List<String>> headers) throws IOException {
        handler.onResponseHeader(httpVersion, responseCode, responseMessage, new LinkedHashMap<>(headers));
    }

    protected void manageRequestheader(H handler, HttpInput input, String method, String url, String httpVersion,
            Map<String, List<String>> headers) throws IOException {
        handler.onRequestHeader(method, url, httpVersion, new LinkedHashMap<>(headers));
    }

    protected void manageError() {
        // Does nothing.
    }

    private void appendToBuffer(byte currentByte) {
        buffer[bufferCount] = currentByte;
        bufferCount++;
    }
}
