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
package com.github.apetrelli.scafa.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HeaderHolder;

public class HttpUtils {
    
    private static final Logger LOG = Logger.getLogger(HttpUtils.class.getName());

    private static final byte CR = 13;

    private static final byte LF = 10;

    private HttpUtils() {
    }

    public static Integer sendHeader(HeaderHolder holder, AsynchronousSocketChannel channelToSend) throws IOException {
        return sendBuffer(holder.toHeapByteBuffer(), channelToSend);
    }

    public static Integer flushBuffer(ByteBuffer buffer, AsynchronousSocketChannel channelToSend) throws IOException {
        buffer.flip();
        Integer result = getFuture(channelToSend.write(buffer));
        buffer.clear();
        return result;
    }

    public static Integer sendChunkSize(long size, AsynchronousSocketChannel channelToSend)
            throws IOException {
        String sizeString = Long.toString(size, 16);
        ByteBuffer buffer = ByteBuffer.allocate(sizeString.length() + 2);
        buffer.put(sizeString.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF);
        return flushBuffer(buffer, channelToSend);
    }

    public static Integer sendNewline(AsynchronousSocketChannel channelToSend)
            throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put(CR).put(LF);
        return flushBuffer(buffer, channelToSend);
    }

    public static <T> T getFuture(Future<T> future) throws IOException {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new IOException("Future problem", e);
            }
        }
    }

    private static Integer sendBuffer(ByteBuffer buffer, AsynchronousSocketChannel channelToSend) throws IOException {
        if (LOG.isLoggable(Level.FINEST)) {
            String request = new String(buffer.array(), 0, buffer.limit());
            LOG.finest("-- Raw request/response header");
            LOG.finest(request);
            LOG.finest("-- End of header --");
        }
        return flushBuffer(buffer, channelToSend);
    }

}
