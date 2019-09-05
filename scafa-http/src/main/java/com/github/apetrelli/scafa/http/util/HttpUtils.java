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
package com.github.apetrelli.scafa.http.util;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HeaderHolder;

public class HttpUtils {

    private static final String END_OF_CHUNKED_TRANSFER_SIZE_STRING = "0";

	private static class BufferFlusherCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
        private final CompletionHandler<Void, Void> completionHandler;

        private AsynchronousSocketChannel channel;

        private BufferFlusherCompletionHandler(AsynchronousSocketChannel channel, CompletionHandler<Void, Void> completionHandler) {
            this.channel = channel;
            this.completionHandler = completionHandler;
        }

        @Override
        public void completed(Integer result, ByteBuffer attachment) {
            if (attachment.hasRemaining()) {
                channel.write(attachment, attachment, this);
            } else {
                completionHandler.completed(null, null);
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            completionHandler.failed(exc, null);
        }
    }

    private static final Logger LOG = Logger.getLogger(HttpUtils.class.getName());

    private static final byte CR = 13;

    private static final byte LF = 10;

    private HttpUtils() {
    }

    public static void sendHeader(HeaderHolder holder, AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
        ByteBuffer buffer = holder.toHeapByteBuffer();
        if (LOG.isLoggable(Level.FINEST)) {
            String request = new String(buffer.array(), 0, buffer.limit());
            LOG.finest("-- Raw request/response header");
            LOG.finest(request);
            LOG.finest("-- End of header --");
        }
        flipAndFlushBuffer(buffer, channelToSend, completionHandler);
    }

    public static void flushBuffer(ByteBuffer buffer, AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
        channelToSend.write(buffer, buffer, new BufferFlusherCompletionHandler(channelToSend, completionHandler));
    }

    public static void sendChunkSize(long size, AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
        String sizeString = Long.toString(size, 16);
        ByteBuffer buffer = ByteBuffer.allocate(sizeString.length() + 2);
        buffer.put(sizeString.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF);
        flipAndFlushBuffer(buffer, channelToSend, completionHandler);
    }

    public static void sendNewline(AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put(CR).put(LF);
        flipAndFlushBuffer(buffer, channelToSend, completionHandler);
    }

    public static void sendEndOfChunkedTransfer(AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
        ByteBuffer buffer = ByteBuffer.allocate(END_OF_CHUNKED_TRANSFER_SIZE_STRING.length() + 4);
        buffer.put(END_OF_CHUNKED_TRANSFER_SIZE_STRING.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF).put(CR).put(LF);
        flipAndFlushBuffer(buffer, channelToSend, completionHandler);
    }

    private static void flipAndFlushBuffer(ByteBuffer buffer, AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
        buffer.flip();
        flushBuffer(buffer, channelToSend, completionHandler);
    }
}
