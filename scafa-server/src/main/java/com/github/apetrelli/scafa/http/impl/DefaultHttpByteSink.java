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
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.HttpBodyMode;
import com.github.apetrelli.scafa.http.HttpByteSink;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;

public class DefaultHttpByteSink<H extends HttpHandler> implements HttpByteSink {

    private final static Logger LOG = Logger
            .getLogger(DefaultHttpByteSink.class.getName());

    protected H handler;

    private StringBuilder lineBuilder = new StringBuilder();

    private HttpRequest request;

    private HttpResponse response;

    private HeaderHolder holder;

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
        request = null;
        response = null;
        holder = null;
    }

    @Override
    public void start(HttpInput input) {
        reset();
        lineBuilder.append((char) input.getBuffer().get());
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
                String httpVersion = pieces[0];
                int responseCode;
                try {
                    responseCode = Integer.parseInt(pieces[1]);
                } catch (NumberFormatException e) {
                    responseCode = 500;
                    LOG.log(Level.SEVERE, "The response code is not a number: " + pieces[1], e);
                }
                StringBuilder builder = new StringBuilder();
                builder.append(pieces[2]);
                for (int i = 3; i < pieces.length; i++) {
                    builder.append(" ").append(pieces[i]);
                }
                String responseMessage = builder.toString();
                response = new HttpResponse(httpVersion, responseCode, responseMessage);
                holder = response;
            } else if (pieces.length == 3) {
                request = new HttpRequest(pieces[0], pieces[1], pieces[2]);
                holder = request;
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
            holder.addHeader(key, value);
        } else {
            LOG.severe("The header is invalid: " + header);
        }
        appendToBuffer(currentByte);
    }

    @Override
    public void preEndHeader(HttpInput input) {
        appendToBuffer(input.getBuffer().get());
        input.setBodyMode(HttpBodyMode.EMPTY);
        String lengthString = holder.getHeader("CONTENT-LENGTH");
        if (lengthString != null) {
            try {
                long length = Long.parseLong(lengthString.trim());
                if (length > 0L) {
                    input.setBodyMode(HttpBodyMode.BODY);
                    input.setBodySize(length);
                }
            } catch (NumberFormatException e) {
                LOG.log(Level.SEVERE, "The provided length is not an integer: " + lengthString, e);
            }
        } else { // Check chunked transfer
            String encoding = holder.getHeader("TRANSFER-ENCODING");
            if ("chunked".equals(encoding)) {
                input.setBodyMode(HttpBodyMode.CHUNKED);
            }
        }
    }

    @Override
    public void endHeader(HttpInput input, CompletionHandler<Void, Void> completionHandler) {
        appendToBuffer(input.getBuffer().get());
        clearLineBuilder();
        if (request != null) {
            manageRequestHeader(handler, input, request, completionHandler);
        } else if (response != null) {
            try {
				handler.onResponseHeader(new HttpResponse(response));
			} catch (IOException e) {
				completionHandler.failed(e, null);
			}
        }
    }

    @Override
    public void endHeaderAndRequest(HttpInput input, CompletionHandler<Void, Void> completionHandler) {
    	endHeader(input, new CompletionHandler<Void, Void>() {

			@Override
			public void completed(Void result, Void attachment) {
				try {
					handler.onEnd();
					completionHandler.completed(result, attachment);
				} catch (IOException e) {
					completionHandler.failed(e, attachment);
				}
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				completionHandler.failed(exc, attachment);
			}
		});

    }

    @Override
    public void afterEndOfChunk(byte currentByte) throws IOException {
        handler.onChunkEnd();
    }

    @Override
    public void beforeChunkCount(byte currentByte) {
        clearLineBuilder();
    }

    @Override
    public void appendChunkCount(byte currentByte) {
        lineBuilder.append((char) currentByte);
    }

    @Override
    public void preEndChunkCount(byte currentByte) {
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

    protected void manageRequestHeader(H handler, HttpInput input, HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        handler.onRequestHeader(new HttpRequest(request), completionHandler);
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
