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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpBodyMode;
import com.github.apetrelli.scafa.http.HttpByteSink;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpInput;

public class DefaultHttpByteSink<H extends HttpHandler> implements HttpByteSink {

    private final static Logger LOG = Logger
            .getLogger(DefaultHttpByteSink.class.getName());

    protected H handler;
    
    private StringBuilder lineBuilder = new StringBuilder();

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
        clearLineBuilder();
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
        clearLineBuilder();
        lineBuilder.append((char) input.getBuffer().get());
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
        String requestLine = lineBuilder.toString();
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
        clearLineBuilder();
    }

    @Override
    public void beforeHeaderLine(byte currentByte) {
        appendToBuffer(currentByte);
        clearLineBuilder();
    }

    @Override
    public void appendHeader(byte currentByte) {
        appendToBuffer(currentByte);
    }

    @Override
    public void endHeaderLine(byte currentByte) {
        String header = lineBuilder.toString();
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
        clearLineBuilder();
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
    public void afterEndOfChunk(byte currentByte) throws IOException {
        handler.onChunkEnd();
    }

    @Override
    public void beforeChunkCount(byte currentByte) throws IOException {
        clearLineBuilder();
    }

    @Override
    public void appendChunkCount(byte currentByte) throws IOException {
        lineBuilder.append((char) currentByte);
    }

    @Override
    public void preEndChunkCount(byte currentByte) throws IOException {
        LOG.finest("Pre End chunk count");
    }

    @Override
    public void endChunkCount(HttpInput input) throws IOException {
        input.getBuffer().get(); // Skipping LF.
        if (lineBuilder.length() > 0) {
            String chunkCountHex = lineBuilder.toString();
            try {
                long chunkCount = Long.parseLong(chunkCountHex, 16);
                LOG.log(Level.FINEST, "Preparing to read {0} bytes of a chunk", chunkCount);
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
        ByteBuffer buffer = input.getBuffer();
        int oldLimit = buffer.limit();
        int size = oldLimit - buffer.position();
        int sizeToSend = (int) Math.min(size, input.getCountdown());
        buffer.limit(buffer.position() + sizeToSend);
        if (!input.isCaughtError()) {
            handler.onBody(buffer, input.getBodyOffset(), input.getBodySize());
        }
        input.reduceBody(sizeToSend);
        buffer.limit(oldLimit);
        if (!input.isHttpConnected() && input.getCountdown() <= 0L) {
            handler.onEnd();
            if (input.isCaughtError()) {
                manageError();
            }
        }
    }

    @Override
    public void chunkedTransferLastCr(byte currentByte) {
        LOG.finest("Chunked transfer last CR");
    }

    @Override
    public void chunkedTransferLastLf(byte currentByte) {
        LOG.finest("Chunked transfer last LF");
    }

    @Override
    public void sendChunkData(HttpInput input) throws IOException {
        if (!input.isCaughtError()) {
            ByteBuffer buffer = input.getBuffer();
            int bufferSize = buffer.limit() - buffer.position();
            long maxsize = input.getCountdown();
            if (bufferSize > maxsize) {
                bufferSize = (int) maxsize;
            }
            handler.onChunk(buffer.array(), buffer.position(), new Long(bufferSize).intValue(),
                    input.getTotalChunkedTransferLength() - input.getChunkLength(), input.getChunkOffset(),
                    input.getChunkLength());
            buffer.position(buffer.position() + bufferSize);
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "Handling chunk from {0} to {1}",
                        new Object[] { input.getChunkOffset(), input.getChunkOffset() + bufferSize });
            }
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
        lineBuilder.append((char) currentByte);
    }

    private void clearLineBuilder() {
        lineBuilder.delete(0, lineBuilder.length());
    }
}
